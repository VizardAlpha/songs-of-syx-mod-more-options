package com.github.argon.sos.moreoptions.phase;

/**
 * See {@link Phases} for more.
 */
public enum Phase {
    // 1. Phase
    INIT_BEFORE_GAME_CREATED,
    // 2. Phase
    INIT_MOD_CREATE_INSTANCE,
    // 3. Phase
    ON_GAME_SAVE_LOADED,
    // 5. Phase
    ON_GAME_SAVE_RELOADED,
    // 6. Phase
    INIT_NEW_GAME_SESSION,
    // 7. Phase
    INIT_GAME_UPDATING,
    // 8. Phase
    ON_GAME_UPDATE,
    // 9. Phase
    INIT_GAME_UI_PRESENT,
    ON_GAME_SAVED,
    ON_CRASH
}
