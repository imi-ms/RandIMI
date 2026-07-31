package de.unimuenster.imi.randimi.model.enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Type of interaction for the audit.
 *
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
public enum AuditType {
	READ("READ"),
	CREATE("CREATE"),
	UPDATE("UPDATE"),
	DELETE("DELETE"),
	TEST("TEST"),
	ACTIVATE("ACTIVATE"),
	READ_SUBJECTS("READ_SUBJECTS"),
	RELEASE_SUBJECT("RELEASE_SUBJECT"),
	USERNAME_CHANGE("USERNAME_CHANGE"),
	EXPORT_SUBJECTS("EXPORT_SUBJECTS"),
	LOCK("LOCK"),
	UNLOCK("UNLOCK"),
	ARCHIVE("ARCHIVE"),
	REACTIVATE("REACTIVATE"),
	;

	private final String textValue;
	private static final Map<String, AuditType> stringToEnum = new HashMap<>();

	static // Initialize map from constant name to enum constant
	{
		for (AuditType cValue : values()) {
			stringToEnum.put(cValue.toString(), cValue);
		}
	}

	AuditType(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static AuditType fromString(String textValue) {
		return stringToEnum.get(textValue);
	}
}
