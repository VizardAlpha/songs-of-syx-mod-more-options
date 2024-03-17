package com.github.argon.sos.moreoptions.ui.panel.boosters;

import com.github.argon.sos.moreoptions.config.ConfigDefaults;
import com.github.argon.sos.moreoptions.config.domain.Range;
import com.github.argon.sos.moreoptions.game.ui.*;
import com.github.argon.sos.moreoptions.game.util.UiUtil;
import com.github.argon.sos.moreoptions.i18n.I18n;
import com.github.argon.sos.moreoptions.ui.UiMapper;
import com.github.argon.sos.moreoptions.util.Maps;
import game.boosting.BOOSTABLE_O;
import game.boosting.Boostable;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.race.RACES;
import init.sprite.UI.UI;
import lombok.Getter;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.text.StringInputSprite;
import util.gui.misc.GHeader;
import util.gui.misc.GInput;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoostersSection extends GuiSection implements Valuable<Map<String, Range>, BoostersSection> {
    private static final I18n i18n = I18n.get(BoostersPanel.class);

    private final Table<Range> boosterTable;

    @Getter
    private final Faction faction;

    public BoostersSection(Faction faction, List<BoostersPanel.Entry> boosterEntries, int availableHeight) {
        this.faction = faction;

        Map<String, List<BoostersPanel.Entry>> groupedBoosterEntries = UiMapper.toBoosterPanelEntriesCategorized(boosterEntries);
        Map<String, List<ColumnRow<Range>>> boosterRows = groupedBoosterEntries.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().stream()
                .map(this::boosterRow)
                .collect(Collectors.toList())
        ));

        StringInputSprite searchInput = new StringInputSprite(16, UI.FONT().M).placeHolder(i18n.t("BoostersPanel.search.input.name"));
        GInput search = new GInput(searchInput);

        GHeader titleHeader = new GHeader(faction.name, UI.FONT().H1);
        GuiSection header = new GuiSection();
        header.addRightC(0, faction.banner().HUGE);
        header.addRightC(20, titleHeader);

        int tableHeight = availableHeight - header.body().height() - search.body().height() - 30;
        this.boosterTable = Table.<Range>builder()
            .rowsCategorized(boosterRows)
            .evenOdd(true)
            .highlight(true)
            .search(searchInput)
            .displayHeight(tableHeight)
            .build();

        addDownC(0, header);
        addDownC(10, search);
        addDownC(10, boosterTable);
    }

    private ColumnRow<Range> boosterRow(BoostersPanel.Entry boosterEntry) {
        Boostable boostable = boosterEntry.getBoosters().getAdd().getOrigin();
        Range rangePerc;
        Range rangeAdd;
        String activeKey;

        BOOSTABLE_O bonus;
        if (faction instanceof FactionNPC) {
            bonus = ((FactionNPC) faction).bonus;
        } else {
            bonus = RACES.clP(null, null);
        }

        // Label with hover
        Label boosterLabel = Label.builder()
            .name(boostable.name.toString())
            .maxWidth(300)
            .hoverGuiAction(guiBox -> {
                guiBox.title(faction.name + ": " + boostable.name);
                guiBox.text(boostable.desc);
                guiBox.NL(8);
                boostable.hoverDetailed(guiBox, bonus, null, true);
            })
            .build();

        // Icon
        GuiSection icon = UiUtil.toGuiSection(new RENDEROBJ.Sprite(boostable.icon));

        if (boosterEntry.getRange().getApplyMode().equals(Range.ApplyMode.PERCENT)) {
            rangePerc = boosterEntry.getRange();
            rangeAdd = ConfigDefaults.boosterAdd();
            activeKey = "perc";
        } else {
            rangePerc = ConfigDefaults.boosterPercent();
            rangeAdd = boosterEntry.getRange();
            activeKey = "add";
        }

        Slider multiSlider = Slider.SliderBuilder.fromRange(rangePerc)
            .input(true)
            .lockScroll(true)
            .threshold((int) (0.10 * rangeAdd.getMax()), COLOR.YELLOW100.shade(0.7d))
            .threshold((int) (0.50 * rangeAdd.getMax()), COLOR.ORANGE100.shade(0.7d))
            .threshold((int) (0.75 * rangeAdd.getMax()), COLOR.RED100.shade(0.7d))
            .threshold((int) (0.90 * rangeAdd.getMax()), COLOR.RED2RED)
            .width(300)
            .build();
        Slider additiveSlider = Slider.SliderBuilder.fromRange(rangeAdd)
            .input(true)
            .lockScroll(true)
            .threshold((int) (0.10 * rangeAdd.getMax()), COLOR.YELLOW100.shade(0.7d))
            .threshold((int) (0.50 * rangeAdd.getMax()), COLOR.ORANGE100.shade(0.7d))
            .threshold((int) (0.75 * rangeAdd.getMax()), COLOR.RED100.shade(0.7d))
            .threshold((int) (0.90 * rangeAdd.getMax()), COLOR.RED2RED)
            .width(300)
            .build();

        // Booster toggle
        Tabulator<String, Slider, Integer> slidersWithToggle = Tabulator.<String, Slider, Integer>builder()
            .tabs(Maps.ofLinked(
                "perc", multiSlider,
                "add", additiveSlider
            ))
            .tabMenu(Toggler.<String>builder()
                .menu(ButtonMenu.<String>builder()
                    .button("add", new Button(i18n.t("BoostersPanel.booster.toggle.add.name"), i18n.t("BoostersPanel.booster.toggle.add.desc")))
                    .button("perc", new Button(i18n.t("BoostersPanel.booster.toggle.perc.name"), i18n.t("BoostersPanel.booster.toggle.perc.desc")))
                    .horizontal(true)
                    .sameWidth(true)
                    .build())
                .aktiveKey(activeKey)
                .highlight(true)
                .build())
            .resetOnToggle(true)
            .direction(DIR.W)
            .build();

        return ColumnRow.<Range>builder()
            .key(boosterEntry.getKey())
            .column(boosterLabel)
            .column(icon)
            .column(slidersWithToggle)
            .searchTerm(boostable.name.toString())
            .valueConsumer(range -> slidersWithToggle.setValue(range.getValue()))
            .valueSupplier(() -> Range.fromSlider(slidersWithToggle.getActiveTab()))
            .build();
    }

    @Override
    public Map<String, Range> getValue() {
        return boosterTable.getValue();
    }

    @Override
    public void setValue(Map<String, Range> boosterValues) {
        boosterTable.setValue(boosterValues);
    }
}
