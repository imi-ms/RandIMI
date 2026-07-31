package de.unimuenster.imi.randimi.model.enumeration;

import de.unimuenster.imi.randimi.model.SelectInputOption;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum used for pseudonyms in studies.
 * 
 * @author Tobias Hardt
 */
public enum PseudonymHandling implements SelectInputOption {
	UNIQUE_IN_STUDY ("UNIQUE_IN_STUDY"), // pseudonym must be unique in the study
	UNIQUE_IN_LOCATION("UNIQUE_IN_LOCATION"); // pseudonym must be unique for the location. The same pseudonym in each location is allowed.
	
	private String textValue;
	private static final Map<String, PseudonymHandling> stringToEnum = new HashMap<>();

	static // Initialize map from constant name to enum constant
	{
		for (PseudonymHandling cValue : values()) {
			stringToEnum.put(cValue.toString(), cValue);
		}
	}

	PseudonymHandling(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public boolean lookupTranslation() {
		return true;
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static PseudonymHandling fromString(String textValue) {
		return stringToEnum.get(textValue);
	}

	@Override
	public String getOptionName() {
		return textValue;
	}

	@Override
	public String getOptionValue() {
		return textValue;
	}
}
