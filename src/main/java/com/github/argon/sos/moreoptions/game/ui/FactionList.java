package com.github.argon.sos.moreoptions.game.ui;

import com.github.argon.sos.moreoptions.game.action.Action;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import lombok.Setter;
import lombok.experimental.Accessors;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.DOUBLE;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import world.WORLD;
import world.WorldMinimap;
import world.map.regions.Region;
import world.region.RD;

public class FactionList extends GuiSection {

	private final ArrayList<Faction> sorted = new ArrayList<>(FACTIONS.MAX);
	
	private final int width = C.SG*264;
	private int[] emmi = new int[FACTIONS.MAX];
	
	private final StringInputSprite filter = new StringInputSprite(20, UI.FONT().S);
	private final GTableBuilder builder;
	private final GETTER_IMP<Faction> getter;

	@Setter
	@Accessors(fluent = true, chain = false)
	private Action<Faction> clickAction = o -> {};

	private final Tree<Faction> sorter = new Tree<Faction>(FACTIONS.MAX) {

		@Override
		protected boolean isGreaterThan(Faction current, Faction cmp) {
			return value(current) > value(cmp);
		}
		
		private int value(Faction f) {
			if (DIP.WAR().is(FACTIONS.player(), f))
				return 0+f.index();
			if (DIP.TRADE().is(FACTIONS.player(), f))
				return FACTIONS.MAX+f.index();
			if (RD.DIST().reachable(f))
				return FACTIONS.MAX*2+f.index();
			if (RD.DIST().neighbours(f))
				return FACTIONS.MAX*3+f.index();
			return FACTIONS.MAX*4+f.index();
		}
		
	};
	
	
	public FactionList(GETTER_IMP<Faction> getter, int HEIGHT) {
		this.getter = getter;
		// default player
		getter.set(FACTIONS.player());

		ClickableAbs minimap = new ClickableAbs(WorldMinimap.WIDTH + 6, WorldMinimap.HEIGHT + 6) {

			private Faction hf;

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				GCOLOR.UI().borderH(r, body, 0);
				WORLD.MINIMAP().render(r, body().x1() + 3, body.y1() + 3);
			}

			@Override
			public boolean hover(COORDINATE mCoo) {
				hf = null;
				if (super.hover(mCoo)) {
					int x = mCoo.x() - body.x1() - 3;
					int y = mCoo.y() - body.y1() - 3;
					x = (WORLD.TWIDTH() * x) / WorldMinimap.WIDTH;
					y = (WORLD.THEIGHT() * y) / WorldMinimap.HEIGHT;
					if (WORLD.TBOUNDS().holdsPoint(x, y)) {
						Region reg = WORLD.REGIONS().map.get(x, y);
						if (reg != null && reg.faction() != null) {
							hf = reg.faction();
							WORLD.MINIMAP().hilight(hf);
						}
					}
					return true;
				}
				return false;
			}

			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (hf != null) {
					VIEW.UI().factions.hover(text, hf);

				}
			}

			@Override
			protected void clickA() {
				if (hf != null && hf instanceof FactionNPC) {
					getter.set((FactionNPC) hf);
					builder.set(sorted.indexOf(getter.get()));
				}
				super.clickA();
			}

		};

		filter.placeHolder(DicMisc.¤¤Search);
		Input search = new Input(filter);


		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return sorted.size();
			}			
		};
		builder.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new FactionNPCButton(ier);
			}
		});


		Button playerButton = new Button("You: " + FACTIONS.player().name, "Your faction");
		playerButton.clickActionSet(() -> {
			getter.set(FACTIONS.player());
			clickAction.accept(FACTIONS.player());
		});
		playerButton.renActionSet(() -> {
			boolean selected = FACTIONS.player().equals(getter.get());
			playerButton.selectedSet(selected);
		});


		GuiSection factionList = builder.createHeight(
			HEIGHT - 40 - minimap.body().height() - search.body.height() - playerButton.body.height(),
			false);

		playerButton.body().setWidth(factionList.body().width());

		addDownC(0, minimap);
		addDownC(8, search);
		addDownC(16, playerButton);
		addDownC(16, factionList);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		sorted.clear();
		for (FactionNPC f : FACTIONS.NPCs())
			sorter.add(f);
		while(sorter.hasMore()) {
			Faction f = sorter.pollSmallest();
			if (filter.text().length() > 0) {
				if (f.name.containsText(filter.text()))
					sorted.add(f);
			}else
				sorted.add(f);
		}


		super.render(r, ds);
	}
	
	
	private final class FactionNPCButton extends GuiSection {
		
		private final GETTER<Integer> ier;
		
		FactionNPCButton(GETTER<Integer> ier){
			this.ier = ier;
			
			RENDEROBJ o;
			
			o = new RenderImp(Icon.L*2+16, Icon.L*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					FactionNPC f = getFactionNPC();
					if (f == null)
						return;
					f.banner().HUGE.render(r,  body().x1(), body().y1());
					Royalty ro = f.court().king().roy();
					int x1 = body().x1()+Icon.L+Icon.L/2;
					int y1 = body().y1()+16;
					STATS.APPEARANCE().portraitRender(r, ro.induvidual, x1, y1, 1);
					ro.induvidual.race().appearance().crown.all().get(0).renderScaled(r, x1, y1, 1);
				}
			};

			add(o);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					Faction f = getFaction();
					if (f != null)
						text.lablifySub().add(f.name);
				}
			}.r(DIR.NW);
			add(o, getLastX2()+12, 4);
			
			o = new GStat() {
				
				@Override
				public void update(GText text) {
					Faction f = getFaction();
					if (f == null)
						return;
					
					int am = RD.RACES().population.faction().get(f);
					GFORMAT.i(text, am);
				}
			}.hh(SPRITES.icons().s.human);
			add(o, getLastX1(), getLastY2()+4);
			addRightC(55, new GStat() {
				
				@Override
				public void update(GText text) {
					Faction f = getFaction();
					if (f == null)
						return;
					GFORMAT.i(text, emmi[f.index()]);
				}
			}.hh(UI.icons().s.flags));
			
			
			add(GMeter.sprite(GMeter.C_REDGREEN, new DOUBLE() {

				@Override
				public double getD() {
					FactionNPC f = getFactionNPC();
					if (f == null)
						return 0;
					return RD.RACES().population.faction().get(f)/(10*RD.RACES().maxPop());
				}
				
			}, 100, 12), o.body().x1(), getLastY2()+1);
			
			add(GMeter.sprite(GMeter.C_ORANGE, new DOUBLE() {

				@Override
				public double getD() {
					Faction f = getFaction();
					if (f == null)
						return 0;
					return RD.RACES().population.faction().get(f)/(10*RD.RACES().maxPop());
				}
				
			}, 100, 12), getLastX1(), getLastY2()+1);
			
			pad(8, 6);
			body().setWidth(width);

			o = new RENDEROBJ.Sprite(UI.icons().s.money) {

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					Faction f = getFaction();
					if (f == null)
						return;
					if (!DIP.TRADE().is(FACTIONS.player(), f)) {
						return;
					}

					if (!RD.DIST().reachable(f)) {
						OPACITY.O50.bind();
					}
					super.render(r, ds);
					OPACITY.unbind();
				}

			};
			o.body().moveX2(body().x2()-8);
			o.body().moveY1(8);
			add(o);

		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {

			boolean hovered = hoveredIs();
			FactionNPC f = getFactionNPC();
			boolean selected = getter.get() == f;
			boolean active = f.capitolRegion() != null;

			if (hovered || selected)
				WORLD.MINIMAP().hilight(f);

			GButt.ButtPanel.renderBG(r, active, selected, hovered, body());

			if (DIP.WAR().is(FACTIONS.player(), f)) {
				OPACITY.O25.bind();
				COLOR.RED100.render(r, body(),-4);
				OPACITY.unbind();
			}

			super.render(r, ds);

			if (!RD.DIST().reachable(f)) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body(),-4);
				OPACITY.unbind();
			}else {
				DIP.get(f).icon.render(r, body().x2()-18, body().y1()+2);
			}

			GButt.ButtPanel.renderFrame(r, body());
			
			
		}
		
		@Override
		protected void clickA() {
			Faction faction = getFaction();
			getter.set(faction);
			clickAction.accept(faction);
		}
		
		private Faction getFaction() {
			return sorted.get(ier.get());
		}

		private FactionNPC getFactionNPC() {
			return (FactionNPC) sorted.get(ier.get());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
			if (text.emptyIs())
				VIEW.UI().factions.hover(text, getFaction());
			
		}

	}
}
