package de.unimuenster.imi.randimi.model.enumeration;

import java.util.*;
import java.util.stream.Collectors;

import de.unimuenster.imi.randimi.model.SelectInputOption;
import lombok.Getter;

/**
 * Languages supported by RandIMI.
 *  
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */
public enum SupportedLanguage implements SelectInputOption {
	GERMAN("de_DE", "de-DE", "Deutsch"),
	ENGLISH("en_US", "en-US", "English");

	/**
	 * Dynamically assigns each
	 */
	public static final Map<SupportedLanguage, Integer> INDICES = new HashMap<>();

	static {
		int index = 0;
		for (SupportedLanguage supportedLanguage : values()) {
			INDICES.put(supportedLanguage, index);
			index += 1;
		}
	}

	@Getter
	private final String tag;
	@Getter
	private final String isoValue;
	@Getter
	private final String nativeDisplayName;
	
	/**
	 * Constructor
	 * 
	 * @param isoValue ISO Value e.g. de_DE.
	 * @param nativeDisplayName Display name in native language.
	 */
	SupportedLanguage(final String tag, final String isoValue, final String nativeDisplayName) {
		this.tag = tag;
		this.isoValue = isoValue;
		this.nativeDisplayName = nativeDisplayName;
	}

	public String getLanguage() {
		return tag.substring(0, 2);
	}

	public String getCountry() {
		return tag.substring(3, 5);
	}

	public Locale toLocale() {
		return new Locale(getLanguage(), getCountry());
	}

	/**
	 * Returns the SupportedLanguage by its ISO value.
	 * 
	 * @param isoValue ISO value to check.
	 * @return The associated SupportedLanguage.
	 */
	public static SupportedLanguage fromIsoValue(final String isoValue) {
		return Arrays.stream(SupportedLanguage.values())
					 .filter(e -> e.isoValue.equals(isoValue))
					 .findFirst().orElse(null);
	}

	public static List<Locale> getSupportedLocals() {
		return Arrays.stream(SupportedLanguage.values()).map(SupportedLanguage::toLocale).toList();
	}

	@Override public boolean lookupTranslation() {
		return false;
	}

	@Override public String getOptionName() {
		return nativeDisplayName;
	}

	@Override public String getOptionValue() {
		return name();
	}
}

