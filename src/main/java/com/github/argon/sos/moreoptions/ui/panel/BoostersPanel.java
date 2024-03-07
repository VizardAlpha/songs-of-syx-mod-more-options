package com.github.argon.sos.moreoptions.ui.panel;

import com.github.argon.sos.moreoptions.config.ConfigDefaults;
import com.github.argon.sos.moreoptions.config.MoreOptionsV2Config;
import com.github.argon.sos.moreoptions.game.booster.MoreOptionsBoosters;
import com.github.argon.sos.moreoptions.game.ui.*;
import com.github.argon.sos.moreoptions.i18n.I18n;
import com.github.argon.sos.moreoptions.log.Logger;
import com.github.argon.sos.moreoptions.log.Loggers;
import com.github.argon.sos.moreoptions.ui.UiMapper;
import com.github.argon.sos.moreoptions.util.Maps;
import com.github.argon.sos.moreoptions.util.UiUtil;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.race.RACES;
import init.sprite.UI.UI;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.text.StringInputSprite;
import util.gui.misc.GInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains sliders to influence values of game boosters
 */
public class BoostersPanel extends GuiSection implements Valuable<Map<String, MoreOptionsV2Config.Range>, BoostersPanel> {
    private static final Logger log = Loggers.getLogger(BoostersPanel.class);
    private static final I18n i18n = I18n.get(BoostersPanel.class);
    @Getter
    private final Map<String, Tabulator<String, Slider, Integer>> slidersWithToggle = new HashMap<>();

    public BoostersPanel(List<Entry> boosterEntries) {
        Map<String, List<Entry>> groupedBoosterEntries = UiMapper.toBoosterPanelEntriesCategorized(boosterEntries);
        Map<String, List<ColumnRow<Integer>>> collect = groupedBoosterEntries.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().stream()
                .map(boosterEntry -> {
                    Tabulator<String, Slider, Integer> slider = toSliderWithToggle(boosterEntry);
                    slidersWithToggle.put(entry.getKey(), slider);
                    return toColumnRow(boosterEntry, slider);
                })
                .collect(Collectors.toList())
        ));

        StringInputSprite searchInput = new StringInputSprite(16, UI.FONT().M).placeHolder(i18n.t("BoostersPanel.search.input.name"));
        Table<Integer> boosterTable = Table.<Integer>builder()
            .rowsCategorized(collect)
            .evenOdd(true)
            .search(searchInput)
            .displayHeight(500)
            .build();

        addDownC(0, new GInput(searchInput));
        addDownC(10, boosterTable);
    }

    @Override
    public Map<String, MoreOptionsV2Config.Range> getValue() {
        return slidersWithToggle.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                tab -> MoreOptionsV2Config.Range.fromSlider(tab.getValue().getActiveTab())));
    }

    private Tabulator<String, Slider, Integer> toSliderWithToggle(Entry boosterEntry) {
        MoreOptionsV2Config.Range rangeMulti;
        MoreOptionsV2Config.Range rangeAdd;
        String activeKey;
        if (boosterEntry.getRange().getApplyMode().equals(MoreOptionsV2Config.Range.ApplyMode.MULTI)) {
            rangeMulti = boosterEntry.getRange();
            rangeAdd = ConfigDefaults.boosterAdd();
            activeKey = "multi";
        } else {
            rangeMulti = ConfigDefaults.boosterMulti();
            rangeAdd = boosterEntry.getRange();
            activeKey = "add";
        }

        Slider multiSlider = Slider.SliderBuilder.fromRange(rangeMulti)
            .input(true)
            .threshold((int) (0.10 * rangeAdd.getMax()), COLOR.YELLOW100.shade(0.7d))
            .threshold((int) (0.50 * rangeAdd.getMax()), COLOR.ORANGE100.shade(0.7d))
            .threshold((int) (0.75 * rangeAdd.getMax()), COLOR.RED100.shade(0.7d))
            .threshold((int) (0.90 * rangeAdd.getMax()), COLOR.RED2RED)
            .width(300)
            .build();
        Slider additiveSlider = Slider.SliderBuilder.fromRange(rangeAdd)
            .input(true)
            .threshold((int) (0.10 * rangeAdd.getMax()), COLOR.YELLOW100.shade(0.7d))
            .threshold((int) (0.50 * rangeAdd.getMax()), COLOR.ORANGE100.shade(0.7d))
            .threshold((int) (0.75 * rangeAdd.getMax()), COLOR.RED100.shade(0.7d))
            .threshold((int) (0.90 * rangeAdd.getMax()), COLOR.RED2RED)
            .width(300)
            .build();

        // Booster toggle
        Tabulator<String, Slider, Integer> slidersWithToggle = new Tabulator<>(Maps.ofLinked(
            UiInfo.<String>builder()
                .key("add")
                .title("Add")
                .description("Adds to the booster value.")
                .build(), additiveSlider,
            UiInfo.<String>builder()
                .key("multi")
                .title("Perc")
                .description("Regulates the percentage of the booster value. Values under 100% will lower the effect.")
                .build(), multiSlider
        ), DIR.W, 0, 0, false, true);
        slidersWithToggle.tab(activeKey);

        return slidersWithToggle;
    }

    private ColumnRow<Integer> toColumnRow(Entry boosterEntry, Tabulator<String, Slider, Integer> slidersWithToggle) {
        Boostable boostable = boosterEntry.getBoosters().getAdd().getOrigin();
        Label boosterLabel = Label.builder()
            .name(boostable.name.toString())
            .maxWidth(300)
            .hoverGuiAction(guiBox -> {
                guiBox.title(boostable.name);
                guiBox.text(boostable.desc);
                guiBox.NL(8);
                boostable.hoverDetailed(guiBox, RACES.clP(null, null), null, true);
            })
            .build();

        // Icon
        GuiSection icon = UiUtil.toGuiSection(new RENDEROBJ.Sprite(boostable.icon));

        return ColumnRow.<Integer>builder()
            .column(boosterLabel)
            .column(icon)
            .column(slidersWithToggle)
            .searchTerm(boostable.name.toString())
            .build();
    }

    @Override
    public void setValue(Map<String, MoreOptionsV2Config.Range> config) {
        log.trace("Applying Booster config %s", config);

        config.forEach((key, range) -> {
            if (slidersWithToggle.containsKey(key)) {
                Tabulator<String, Slider, Integer> tabulator = slidersWithToggle.get(key);
                tabulator.reset();
                tabulator.tab(range.getApplyMode().name().toLowerCase());
                tabulator.setValue(range.getValue());

            } else {
                log.warn("No slider with key %s found in UI", key);
            }
        });
    }

    @Data
    @Builder
    @EqualsAndHashCode
    public static class Entry {
        private MoreOptionsV2Config.Range range;
        private String key;

        //  |\__/,|   (`\
        // |_ _  |.--.) )
        // ( T   )     /
        //(((^_(((/(((_/
        @Builder.Default
        private BoostableCat cat = null;

        private MoreOptionsBoosters boosters;
    }
}
