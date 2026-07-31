package de.unimuenster.imi.randimi.model.enumeration;

import de.unimuenster.imi.randimi.model.SelectInputOption;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for the different algorithms used for randomization.
 *
 * @author Tobias Hardt
 */
public enum RandomizationAlgorithm implements SelectInputOption {
	BLOCKED ("BLOCKED"),
	COINTOSS ("COINTOSS"),
	MINIMIZATION("MINIMIZATION");
//	UPLOAD_LIST ("UPLOAD_LIST");

	private String textValue;
	private static final Map<String, RandomizationAlgorithm> stringToEnum = new HashMap<>();

	static // Initialize map from constant name to enum constant
	{
		for (RandomizationAlgorithm cValue : values()) {
			stringToEnum.put(cValue.toString(), cValue);
		}
	}

	RandomizationAlgorithm(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static RandomizationAlgorithm fromString(String textValue) {
		return stringToEnum.get(textValue);
	}

	@Override
	public boolean lookupTranslation() {
		return true;
	}

	@Override
	public String getOptionName() {
		return "enum.RandomizationAlgorithm." + textValue;
	}

	@Override
	public String getOptionValue() {
		return textValue;
	}
}
