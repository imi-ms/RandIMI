package de.unimuenster.imi.randimi.model.enumeration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Preciado-Marquez
 */
public enum SubjectStatus {
	PRE_GENERATED("PRE_GENERATED"),
	ACTIVE("ACTIVE"),
	RELEASED("RELEASED"),
	DELETED("DELETED");

	private String textValue;
	private static final Map<String, SubjectStatus> stringToEnum = new HashMap<>();

	static // Initialize map from constant name to enum constant
	{
		for (final SubjectStatus subjectStatus : values())
			stringToEnum.put(subjectStatus.toString(), subjectStatus);
	}

	public final static List<SubjectStatus> BLOCKING_STATUS = List.of(ACTIVE, DELETED);

	private SubjectStatus(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static SubjectStatus fromString(String textValue) {
		return stringToEnum.get(textValue);
	}
}
