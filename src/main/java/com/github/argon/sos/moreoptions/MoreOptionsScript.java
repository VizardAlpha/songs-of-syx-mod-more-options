package com.github.argon.sos.moreoptions;

import com.github.argon.sos.moreoptions.config.ConfigStore;
import com.github.argon.sos.moreoptions.config.MoreOptionsV2Config;
import com.github.argon.sos.moreoptions.game.SCRIPT;
import com.github.argon.sos.moreoptions.game.api.GameApis;
import com.github.argon.sos.moreoptions.game.ui.Modal;
import com.github.argon.sos.moreoptions.init.InitPhases;
import com.github.argon.sos.moreoptions.init.Initializer;
import com.github.argon.sos.moreoptions.log.Level;
import com.github.argon.sos.moreoptions.log.Logger;
import com.github.argon.sos.moreoptions.log.Loggers;
import com.github.argon.sos.moreoptions.ui.BackupDialog;
import com.github.argon.sos.moreoptions.ui.UiConfig;
import com.github.argon.sos.moreoptions.ui.UiFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snake2d.Errors;
import util.info.INFO;

import java.nio.file.Path;
import java.util.Optional;


/**
 * Entry point for the mod.
 * See {@link SCRIPT} for some documentation.
 */
@NoArgsConstructor
@SuppressWarnings("unused") // used by the game via reflection
public final class MoreOptionsScript implements SCRIPT, InitPhases {

	private final static Logger log = Loggers.getLogger(MoreOptionsScript.class);

	public final static INFO MOD_INFO = new INFO("More Options", "Adds more options to the game :)");

	@Getter
	private final ConfigStore configStore = ConfigStore.getInstance();
	@Getter
	private final MoreOptionsConfigurator configurator = MoreOptionsConfigurator.getInstance();
	@Getter
	private final GameApis gameApis = GameApis.getInstance();
	private final Initializer initializer = Initializer.getInstance();

	@Getter
	private final Dictionary dictionary = Dictionary.getInstance();

	@Getter
	private final UiConfig uiConfig = UiConfig.getInstance();
	private final UiFactory uiFactory = UiFactory.getInstance();

	private Instance instance;

	public final static Level LOG_LEVEL_DEFAULT = Level.TRACE;

	@Override
	public CharSequence name() {
		return MOD_INFO.name;
	}

	@Override
	public CharSequence desc() {
		return MOD_INFO.desc;
	}

	static {
		Loggers.setLevels(LOG_LEVEL_DEFAULT);
	}

	@Override
	public void initBeforeGameCreated() {
		initializer.initBeforeGameCreated();

		// determine log level
		String logLevelName = System.getenv("MO.LOG_LEVEL");
		Level level = Optional.ofNullable(logLevelName)
			.flatMap(Level::fromName)
			.orElseGet(() -> configStore.getMetaInfo().map(
				MoreOptionsV2Config.Meta::getLogLevel)
				.orElse(LOG_LEVEL_DEFAULT));
		Loggers.setLevels(level);

		// custom error handling
		Errors.setHandler(new MoreOptionsErrorHandler<>(this));
	}

	@Override
	public void initCreateInstance() {
		initializer.initCreateInstance();
	}


	/**
	 * BUG!: Method will be executed TWICE by the game
	 * (will be fixed in v65 =))
	 */
	@Override
	public SCRIPT_INSTANCE createInstance() {
		if (instance == null) {
			initCreateInstance();
			log.debug("Creating Mod Instance");

			// try to get current config and merge with defaults; or use whole defaults
			MoreOptionsV2Config config = configStore.getCurrentConfig();

			// add description from game boosters
			gameApis.booster().getBoosters()
				.values().forEach(moreOptionsBoosters -> dictionary.add(moreOptionsBoosters.getAdd()));

			instance = new Instance(this);
		}

		// or else the init methods won't be called again when a save game is loaded
		instance.reset();
		return instance;
	}

	@Override
	public void initGameRunning() {
		initializer.initGameRunning();
	}

	@Override
	public void initGameUiPresent() {
		initializer.initGameUiPresent();

		Modal<BackupDialog> backupDialog = uiConfig.getBackupDialog();
		// show backup dialog?
		if (backupDialog != null) {
			backupDialog.show();
		} else {
			// apply loaded config
			configurator.applyConfig(configStore.getCurrentConfig());
		}

		// FIXME
		//      * NOTHING

		// TODO
		//     * further testing

		// TODO FUTURE
		//  	is there a better way to streamline the process of adding new ui elements with their data and config?
		//      * better config mapping? Race Interaction JsonE and ObjectMapper?
		//      * easier mapping? mapstruct?

		// TODO add a MoreOptionsViewModel inbetween?
		// TODO experimental
//		gameApis.weatherApi().lockDayCycle(1, true);
	}

	@Override
	public void initGameSaveLoaded(Path saveFilePath) {
		initializer.initGameSaveLoaded(saveFilePath);
	}

	@Override
	public void initGameSaved(Path saveFilePath) {
		initializer.initGameSaved(saveFilePath);
	}

	@Override
	public void initNewGameSession() {
		initializer.initNewGameSession();
	}

	@Override
	public void initGameSaveReloaded() {
		initializer.initGameSaveReloaded();
	}

	@Override
	public void update(double seconds) {
	}

	@Override
	public void crash(Throwable throwable) {
		log.warn("Game crash detected!");
		log.info("", throwable);
		initializer.crash(throwable);

		try {
			// backup and delete config file
			if (!configStore.createBackupConfig()) {
				log.warn("Could not create backup config for game crash");
			}
		} catch (Exception e) {
			log.error("Something bad happened while trying to handle game crash", e);
		}
	}
}