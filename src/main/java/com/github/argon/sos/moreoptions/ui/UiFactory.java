package com.github.argon.sos.moreoptions.ui;


import com.github.argon.sos.moreoptions.config.ConfigStore;
import com.github.argon.sos.moreoptions.config.MoreOptionsV3Config;
import com.github.argon.sos.moreoptions.game.api.GameApis;
import com.github.argon.sos.moreoptions.game.ui.*;
import com.github.argon.sos.moreoptions.i18n.I18n;
import com.github.argon.sos.moreoptions.log.Level;
import com.github.argon.sos.moreoptions.log.Logger;
import com.github.argon.sos.moreoptions.log.Loggers;
import com.github.argon.sos.moreoptions.metric.MetricExporter;
import com.github.argon.sos.moreoptions.ui.panel.boosters.BoostersPanel;
import com.github.argon.sos.moreoptions.ui.panel.races.RacesPanel;
import com.github.argon.sos.moreoptions.ui.panel.races.RacesSelectionPanel;
import game.faction.Faction;
import init.paths.ModInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import snake2d.util.color.COLOR;
import util.save.SaveFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Produces new more complex or common UI elements by given configs or from static objects.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UiFactory {

    private static final I18n i18n = I18n.get(UiFactory.class);

    @Getter(lazy = true)
    private final static UiFactory instance = new UiFactory(
        GameApis.getInstance(),
        ConfigStore.getInstance(),
        MetricExporter.getInstance(),
        UiMapper.getInstance()
    );

    private final static Logger log = Loggers.getLogger(UiFactory.class);

    private final GameApis gameApis;
    private final ConfigStore configStore;
    private final MetricExporter metricExporter;
    private final UiMapper uiMapper;

    public FullWindow<MoreOptionsPanel> buildMoreOptionsFullScreen(String title, MoreOptionsV3Config config) {
        log.debug("Building '%s' full screen", title);
        MoreOptionsPanel moreOptionsPanel = buildMoreOptionsPanel(config)
            .availableWidth(FullWindow.FullView.WIDTH)
            .availableHeight(FullWindow.FullView.HEIGHT)
            .build();
        Toggler<String> buttonMenu = moreOptionsPanel.getTabulator().getMenu();

        return new FullWindow<>(title, moreOptionsPanel, buttonMenu);
    }

    public MoreOptionsPanel.MoreOptionsPanelBuilder buildMoreOptionsPanel(MoreOptionsV3Config config) {
        Map<Faction, List<BoostersPanel.Entry>> boosterEntries = uiMapper.toBoosterPanelEntries(config.getBoosters());
        Map<String, List<RacesPanel.Entry>> raceEntries = uiMapper.toRacePanelEntries(config.getRaces().getLikings());

        Set<String> availableStats = gameApis.stats().getAvailableStatKeys();
        ModInfo modInfo = gameApis.mod().getCurrentMod().orElse(null);
        Path exportFolder = MetricExporter.EXPORT_FOLDER;
        Path exportFile = metricExporter.getExportFile();

        return MoreOptionsPanel.builder()
            .config(config)
            .configStore(configStore)
            .raceEntries(raceEntries)
            .availableStats(availableStats)
            .boosterEntries(boosterEntries)
            .modInfo(modInfo)
            .exportFolder(exportFolder)
            .exportFile(exportFile);
    }

    /**
     * Generates race config selection window
     */
    public Window<RacesSelectionPanel> buildRacesConfigSelection(String title) {
        log.debug("Building '%s' ui", title);
        RacesSelectionPanel.Entry current = null;

        // prepare entries
        List<RacesSelectionPanel.Entry> racesConfigs = new ArrayList<>();
        List<ConfigStore.RaceConfigMeta> raceConfigMetas = configStore.loadRacesConfigMetas();
        for (ConfigStore.RaceConfigMeta configMeta : raceConfigMetas) {
            SaveFile saveFile = gameApis.save().findByPathContains(configMeta.getConfigPath()).orElse(null);

            RacesSelectionPanel.Entry entry = RacesSelectionPanel.Entry.builder()
                .configPath(configMeta.getConfigPath())
                .creationDate(configMeta.getCreationTime())
                .updateDate(configMeta.getUpdateTime())
                .saveFile(saveFile)
                .build();
            SaveFile currentFile = gameApis.save().getCurrentFile();

            // is the file the currently active one?
            if (current == null && (
                saveFile != null &&
                currentFile != null &&
                saveFile.fullName.equals(currentFile.fullName)
            )) {
                current = entry;
            }

            racesConfigs.add(entry);
        }

        Window<RacesSelectionPanel> window = new Window<>(title, new RacesSelectionPanel(racesConfigs, current));
        window.center();

        return window;
    }

    public static ButtonMenu.ButtonMenuBuilder<Level> buildLogLevelButtonMenu() {
        double shade = 0.5;
        return ButtonMenu.<Level>builder()
            .button(Level.CRIT, new Button(
                i18n.t("log.level.crit.name"),
                i18n.t("log.level.crit.desc")
            ).bg(COLOR.RED50.shade(shade)))
            .button(Level.ERROR, new Button(
                i18n.t("log.level.error.name"),
                i18n.t("log.level.error.desc")
            ).bg(COLOR.RED200.shade(shade)))
            .button(Level.WARN, new Button(
                i18n.t("log.level.warn.name"),
                i18n.t("log.level.warn.desc")
            ).bg(COLOR.YELLOW100.shade(shade)))
            .button(Level.INFO, new Button(
                i18n.t("log.level.info.name"),
                i18n.t("log.level.info.desc")
            ).bg(COLOR.BLUE100.shade(shade)))
            .button(Level.DEBUG, new Button(
                i18n.t("log.level.debug.name"),
                i18n.t("log.level.debug.desc")
            ).bg(COLOR.GREEN100.shade(shade)))
            .button(Level.TRACE, new Button(
                i18n.t("log.level.trace.name"),
                i18n.t("log.level.trace.desc")
            ).bg(COLOR.WHITE100.shade(shade)));
    }
}
