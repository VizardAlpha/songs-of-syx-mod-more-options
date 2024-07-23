package com.github.argon.sos.moreoptions.game.data.init;

import com.github.argon.sos.moreoptions.game.data.GameFolder;
import com.github.argon.sos.moreoptions.game.data.AbstractGameData;
import init.paths.PATHS;
import lombok.Getter;
import lombok.experimental.Accessors;

public class InitSettlement extends AbstractGameData {
    public InitSettlement() {
        super(PATHS.SETT().init);
    }

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GameFolder environment = folder("environment");
    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GameFolder fence = folder("fence");
    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GameFolder floor = folder("floor");
    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GameFolder fortification = folder("fortification");
    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GameFolder structure = folder("structure");
}
