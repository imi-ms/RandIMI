package de.unimuenster.imi.randimi;

import de.unimuenster.imi.randimi.controller.helper.LocaleHelper;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocaleHelperTest {

    @Test
    public void testProvidedLocaleHasPriority() {
        Locale provided = new Locale("de", "DE");
        String acceptLanguage = "en-US, en;q=0.9, de;q=0.7, *;q=0.5";

        assertEquals(provided, LocaleHelper.getLocale(provided, acceptLanguage),
                     "If a locale is provided next to acceptLanguage it has priority.");
    }

    @Test
    public void testNullLocale() {
        Locale locale = null;
        String acceptLanguage = "de-DE, de;q=0.9, en;q=0.7, *;q=0.5";
        assertEquals(new Locale("de", "DE"), LocaleHelper.getLocale(locale, acceptLanguage),
                     "If locale is null, accept-language is used.");

        acceptLanguage = "en-US, en;q=0.9, de;q=0.7, *;q=0.5";
        assertEquals(new Locale("en", "US"), LocaleHelper.getLocale(locale, acceptLanguage),
                     "If locale is null, accept-language is used.");
    }

    @Test
    public void testFallbackToEnUs() {
        Locale locale = null;
        String acceptLanguage = null;

        assertEquals(new Locale("en", "US"), LocaleHelper.getLocale(locale, acceptLanguage),
                     "If no acceptLanguage and no locale are provided, fall back to en_US");

        acceptLanguage = "fr-FR, fr;q=0.9, *;q=0.5";
        assertEquals(new Locale("en", "US"), LocaleHelper.getLocale(locale, acceptLanguage),
                     "If no suitable acceptLanguage and no locale are provided, fall back to en_US");

        acceptLanguage = "INVALID";
        assertEquals(new Locale("en", "US"), LocaleHelper.getLocale(locale, acceptLanguage),
                     "If no valid acceptLanguage and no locale are provided, fall back to en_US");

        acceptLanguage = "42-42";
        assertEquals(new Locale("en", "US"), LocaleHelper.getLocale(locale, acceptLanguage),
                     "If no valid acceptLanguage and no locale are provided, fall back to en_US");
    }

    @Test
    public void testSingleLocaleValues() {
        String acceptLanguage = "de-DE";

        assertEquals(new Locale("de", "DE"), LocaleHelper.getLocale(null, acceptLanguage),
                     "Parsing single locale values works");
    }

    @Test
    public void testViolatingLocaleEncodingsParsedCorrectly() {
        String violatingAcceptLanguage = "en_gb";

        assertEquals(new Locale("en", "GB"), LocaleHelper.getLocale(null, violatingAcceptLanguage),"We must be able to also parse illformed locale values.");
    }
}
