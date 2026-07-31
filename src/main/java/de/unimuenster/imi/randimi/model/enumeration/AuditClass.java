package de.unimuenster.imi.randimi.model.enumeration;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class of the audit target. Is either STUDY or SUBJECT.
 *
 * @author Tobias Hardt
 */
public enum AuditClass {
	RANDIMI_USER("RANDIMI_USER", List.of(AuditType.USERNAME_CHANGE)),
	STUDY("STUDY", List.of(AuditType.ACTIVATE, AuditType.LOCK, AuditType.UNLOCK, AuditType.CREATE, AuditType.DELETE,
	                       AuditType.EXPORT_SUBJECTS, AuditType.UPDATE, AuditType.READ, AuditType.READ_SUBJECTS,
	                       AuditType.TEST, AuditType.ARCHIVE, AuditType.REACTIVATE)),
	SUBJECT("SUBJECT", List.of(AuditType.CREATE, AuditType.UPDATE, AuditType.DELETE, AuditType.RELEASE_SUBJECT));

	private final String textValue;

	@Getter
	private final List<AuditType> validAuditTypes;

	private static final Map<String, AuditClass> stringToEnum = new HashMap<>();

	static // Initialize map from constant name to enum constant
	{
		for (AuditClass cValue : values()) {
			stringToEnum.put(cValue.toString(), cValue);
		}
	}

	AuditClass(final String textValue, final List<AuditType> validAuditTypes) {
		this.textValue = textValue;
		this.validAuditTypes = validAuditTypes;
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static AuditClass fromString(String textValue) {
		return stringToEnum.get(textValue);
	}
}
