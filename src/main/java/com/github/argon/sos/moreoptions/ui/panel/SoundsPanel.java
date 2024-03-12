package com.github.argon.sos.moreoptions.ui.panel;

import com.github.argon.sos.moreoptions.config.MoreOptionsV2Config;
import com.github.argon.sos.moreoptions.game.ui.Slider;
import com.github.argon.sos.moreoptions.game.ui.Table;
import com.github.argon.sos.moreoptions.game.ui.layout.Layout;
import com.github.argon.sos.moreoptions.game.ui.layout.VerticalLayout;
import com.github.argon.sos.moreoptions.i18n.I18n;
import com.github.argon.sos.moreoptions.log.Logger;
import com.github.argon.sos.moreoptions.log.Loggers;
import com.github.argon.sos.moreoptions.ui.UiMapper;
import util.gui.misc.GHeader;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains slider for controlling the volume of game sound effects
 */
public class SoundsPanel extends AbstractConfigPanel<MoreOptionsV2Config.Sounds, SoundsPanel> {
    private static final Logger log = Loggers.getLogger(SoundsPanel.class);
    private final static I18n i18n = I18n.get(SoundsPanel.class);

    private final Map<String, Slider> ambienceSoundSliders;
    private final Map<String, Slider> settlementSoundSliders;
    private final Map<String, Slider> roomSoundSliders;
    public SoundsPanel(
        String title,
        MoreOptionsV2Config.Sounds sounds,
        MoreOptionsV2Config.Sounds defaultConfig,
        int availableWidth,
        int availableHeight
    ) {
        super(title, defaultConfig);
        this.ambienceSoundSliders = UiMapper.toSliders(sounds.getAmbience());
        this.settlementSoundSliders = UiMapper.toSliders(sounds.getSettlement());
        this.roomSoundSliders = UiMapper.toSliders(sounds.getRoom());

        GHeader ambienceSoundsHeader = new GHeader(i18n.t("SoundsPanel.header.ambienceSounds.name"));
        ambienceSoundsHeader.hoverInfoSet(i18n.d("SoundsPanel.header.ambienceSounds.desc"));

        GHeader settlementSoundsHeader = new GHeader(i18n.t("SoundsPanel.header.settlementSounds.name"));
        settlementSoundsHeader.hoverInfoSet(i18n.d("SoundsPanel.header.settlementSounds.desc"));

        GHeader roomSoundsHeader = new GHeader(i18n.t("SoundsPanel.header.roomSounds.name"));
        roomSoundsHeader.hoverInfoSet(i18n.d("SoundsPanel.header.roomSounds.desc"));

        Layout.vertical(availableHeight)
            .addDown(0, ambienceSoundsHeader)
            .addDown(5, new VerticalLayout.Scalable(150, height -> Table.<Integer>builder()
                .rows(UiMapper.toLabeledColumnRows(ambienceSoundSliders, i18n))
                .rowPadding(5)
                .displayHeight(height)
                .build()))
            .addDown(10, settlementSoundsHeader)
            .addDown(5, new VerticalLayout.Scalable(150, height -> Table.<Integer>builder()
                .rows(UiMapper.toLabeledColumnRows(settlementSoundSliders, i18n))
                .rowPadding(5)
                .displayHeight(height)
                .build()))
            .addDown(10, roomSoundsHeader)
            .addDown(5, new VerticalLayout.Scalable(150, height -> Table.<Integer>builder()
                .rows(UiMapper.toLabeledColumnRows(roomSoundSliders, i18n))
                .rowPadding(5)
                .displayHeight(height)
                .build()))
            .build(this);
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
