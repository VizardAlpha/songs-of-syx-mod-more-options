package com.github.argon.sos.moreoptions.ui.panel;

import com.github.argon.sos.moreoptions.config.MoreOptionsV2Config;
import com.github.argon.sos.moreoptions.game.ui.Slider;
import com.github.argon.sos.moreoptions.game.ui.Table;
import com.github.argon.sos.moreoptions.game.ui.Valuable;
import com.github.argon.sos.moreoptions.log.Logger;
import com.github.argon.sos.moreoptions.log.Loggers;
import com.github.argon.sos.moreoptions.ui.builder.BuildResult;
import com.github.argon.sos.moreoptions.ui.builder.element.LabelBuilder;
import com.github.argon.sos.moreoptions.ui.builder.element.LabeledSliderBuilder;
import com.github.argon.sos.moreoptions.ui.builder.element.SliderBuilder;
import com.github.argon.sos.moreoptions.ui.builder.section.SlidersBuilder;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GHeader;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains slider for controlling the volume of game sound effects
 */
public class SoundsPanel extends GuiSection implements Valuable<MoreOptionsV2Config.Sounds, SoundsPanel> {
    private static final Logger log = Loggers.getLogger(SoundsPanel.class);

    private final Map<String, Slider> ambienceSoundSliders;
    private final Map<String, Slider> settlementSoundSliders;
    private final Map<String, Slider> roomSoundSliders;
    public SoundsPanel(MoreOptionsV2Config.Sounds sounds) {
        BuildResult<Table, Map<String, Slider>> ambienceSlidersResult = SlidersBuilder.builder()
            .displayHeight(150)
            .definitions(sliders(sounds.getAmbience()))
            .build();
        GuiSection ambienceSoundSection = ambienceSlidersResult.getResult();
        this.ambienceSoundSliders = ambienceSlidersResult.getInteractable();

        BuildResult<Table, Map<String, Slider>> settlementSlidersResult = SlidersBuilder.builder()
            .displayHeight(150)
            .definitions(sliders(sounds.getSettlement()))
            .build();
        GuiSection settlementSoundSection = settlementSlidersResult.getResult();
        this.settlementSoundSliders = settlementSlidersResult.getInteractable();

        BuildResult<Table, Map<String, Slider>> roomSlidersResult = SlidersBuilder.builder()
            .displayHeight(150)
            .definitions(sliders(sounds.getRoom()))
            .build();
        GuiSection roomSoundSection = roomSlidersResult.getResult();
        this.roomSoundSliders = roomSlidersResult.getInteractable();

        GuiSection section = new GuiSection();
        GHeader ambienceSoundsHeader = new GHeader("Ambience Sounds");
        ambienceSoundsHeader.hoverInfoSet("Ambience Sounds playing in your settlement");
        section.addDown(0, ambienceSoundsHeader);
        section.addDown(5, ambienceSoundSection);

        GHeader settlementSoundsHeader = new GHeader("Settlement Sounds");
        settlementSoundsHeader.hoverInfoSet("Sounds playing in your settlement");
        section.addDown(10, settlementSoundsHeader);
        section.addDown(5, settlementSoundSection);

        GHeader roomSoundsHeader = new GHeader("Room Sounds");
        roomSoundsHeader.hoverInfoSet("Sounds playing from buildings in your settlement");
        section.addDown(10, roomSoundsHeader);
        section.addDown(5, roomSoundSection);

        addDownC(0, section);
    }

    private Map<String, LabeledSliderBuilder.Definition> sliders(Map<String, MoreOptionsV2Config.Range> slidersConfig) {
        return slidersConfig.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            config -> LabeledSliderBuilder.Definition.builder()
                .labelDefinition(LabelBuilder.Definition.builder()
                    .key(config.getKey())
                    .title(config.getKey())
                    .build())
                .sliderDefinition(SliderBuilder.Definition.fromRange(config.getValue())
                    .maxWidth(300)
                    .build())
                .labelWidth(200)
                .build()));
    }

    @Override
    public MoreOptionsV2Config.Sounds getValue() {
        return MoreOptionsV2Config.Sounds.builder()
            .ambience(getSoundsAmbienceConfig())
            .settlement(getSoundsSettlementConfig())
            .room(getSoundsRoomConfig())
            .build();
    }

    public Map<String, MoreOptionsV2Config.Range> getSoundsAmbienceConfig() {
        return ambienceSoundSliders.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                tab -> MoreOptionsV2Config.Range.fromSlider(tab.getValue())));
    }

    public Map<String, MoreOptionsV2Config.Range> getSoundsSettlementConfig() {
        return settlementSoundSliders.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                tab -> MoreOptionsV2Config.Range.fromSlider(tab.getValue())));
    }

    public Map<String, MoreOptionsV2Config.Range> getSoundsRoomConfig() {
        return roomSoundSliders.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                tab -> MoreOptionsV2Config.Range.fromSlider(tab.getValue())));
    }

    @Override
    public void setValue(MoreOptionsV2Config.Sounds sounds) {
        log.trace("Applying UI sounds config %s", sounds);

        sounds.getAmbience().forEach((key, range) -> {
            if (ambienceSoundSliders.containsKey(key)) {
                ambienceSoundSliders.get(key).setValue(range.getValue());
            } else {
                log.warn("No slider with key %s found in UI", key);
            }
        });

        sounds.getSettlement().forEach((key, range) -> {
            if (settlementSoundSliders.containsKey(key)) {
                settlementSoundSliders.get(key).setValue(range.getValue());
            } else {
                log.warn("No slider with key %s found in UI", key);
            }
        });

        sounds.getRoom().forEach((key, range) -> {
            if (roomSoundSliders.containsKey(key)) {
                roomSoundSliders.get(key).setValue(range.getValue());
            } else {
                log.warn("No slider with key %s found in UI", key);
            }
        });
    }
}
