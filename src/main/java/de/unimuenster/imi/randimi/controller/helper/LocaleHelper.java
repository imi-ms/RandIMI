package de.unimuenster.imi.randimi.controller.helper;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Helper class to deal with locales.
 */
@Service
public class LocaleHelper {

    public static final Locale DEFAULT = new Locale("en", "US");

    public static final Collection<Locale> LOCALES = new HashSet<>(Arrays.asList(
            new Locale("de", "DE"),
            new Locale("en", "US"),
            new Locale("en", "GB")
    ));

    public static Locale getLocale(Locale locale, String acceptLanguage) {
        if (locale != null) {
            return locale;
        }

        if (acceptLanguage == null || acceptLanguage.trim().isEmpty() || acceptLanguage.equals("*")) {
            return DEFAULT;
        }
        // This is a workaround to be able to parse malformed locale tags.
        // Tags of the form 'en_gb' ARE NOT CONFORMANT TO RFC-5646!
        // See https://tools.ietf.org/html/rfc5646#page-4
        acceptLanguage = acceptLanguage.replaceAll("_", "-");

        try {
            List<Locale.LanguageRange> list = Locale.LanguageRange.parse(acceptLanguage);
            locale = Locale.lookup(list, LOCALES);
        } catch (IllegalArgumentException ignored) {
        }

        if (locale != null) {
            return locale;
        }

        return DEFAULT;
    }
}
