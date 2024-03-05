package com.github.argon.sos.moreoptions.i18n;

import com.github.argon.sos.moreoptions.game.api.GameLangApi;
import com.github.argon.sos.moreoptions.init.InitPhases;
import com.github.argon.sos.moreoptions.log.Logger;
import com.github.argon.sos.moreoptions.log.Loggers;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class I18nMessages implements InitPhases {

    @Getter(lazy = true)
    private final static I18nMessages instance = new I18nMessages(
        GameLangApi.getInstance()
    );

    private final static Logger log = Loggers.getLogger(I18nMessages.class);

    private final GameLangApi gameLangApi;

    @Getter
    @Nullable
    private Locale locale;
    @Getter
    @Nullable
    private ResourceBundle messages;

    private final static Locale LOCALE_FALLBACK = GameLangApi.DEFAULT_LOCALE;

    public static ResourceBundle loadMessages(Locale locale) {
        try {
            return ResourceBundle.getBundle("messages", locale);
        } catch (MissingResourceException e) {
            log.debug("No translation messages for locale %s found. Using fallback %s", locale, LOCALE_FALLBACK);
            return ResourceBundle.getBundle("messages", LOCALE_FALLBACK);
        }
    }

    @Override
    public void initBeforeGameCreated() {
        this.locale = gameLangApi.getCurrent();
        log.debug("Initializing localization messages for: %s", locale);
        this.messages = loadMessages(locale);
    }
}
