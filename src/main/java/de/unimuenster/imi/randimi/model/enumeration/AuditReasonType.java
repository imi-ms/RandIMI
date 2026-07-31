package de.unimuenster.imi.randimi.model.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Predefined reasons for audit purpose.
 *
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
public enum AuditReasonType {
	REQUIREMENT_CHANGES("REQUIREMENT_CHANGES", AuditReasonTarget.UPDATE_STUDY),
	BUG_FIX("BUG_FIX", AuditReasonTarget.UPDATE_STUDY),
	MISCONCEPTION("MISCONCEPTION", AuditReasonTarget.DELETE_STUDY),
	TYPO("TYPO", AuditReasonTarget.UPDATE_SUBJECT),
	CONSENT_WITHDRAWN("CONSENT_WITHDRAWN", AuditReasonTarget.DELETE_SUBJECT),
	SUBJECT_DECEASED("SUBJECT_DECEASED", AuditReasonTarget.DELETE_SUBJECT),
	CUSTOM("CUSTOM", AuditReasonTarget.UPDATE_STUDY, AuditReasonTarget.DELETE_STUDY, AuditReasonTarget.UPDATE_SUBJECT, AuditReasonTarget.DELETE_SUBJECT),
	INITIAL_CONFIGURATION("INITIAL_CONFIGURATION", AuditReasonTarget.UPDATE_STUDY),
	NEW_ASSOCIATE("NEW_ASSOCIATE", AuditReasonTarget.UPDATE_STUDY),
	NEW_SITE("NEW_SITE", AuditReasonTarget.UPDATE_STUDY),
	RETENTION_PERIOD("RETENTION_PERIOD", AuditReasonTarget.DELETE_STUDY),
	CREATED_INCORRECTLY("CREATED_INCORRECTLY", AuditReasonTarget.UPDATE_SUBJECT),
	CREATED_BY_ERROR("CREATED_BY_ERROR", AuditReasonTarget.DELETE_STUDY, AuditReasonTarget.DELETE_SUBJECT),
	DOES_NOT_MEET_INCLUSION_CRITERIA("INCLUSION_CRITERIA_NOT_MET", AuditReasonTarget.DELETE_SUBJECT),
	;

	private String textValue;
	private static final Map<String, AuditReasonType> stringToEnum = new HashMap<>();

	static // Initialize map from constant name to enum constant
	{
		for (AuditReasonType cValue : values()) {
			stringToEnum.put(cValue.toString(), cValue);
		}
	}

	AuditReasonType(String textValue) {
		this.textValue = textValue;
	}

	public static boolean isValid(String enumString) {
		return Arrays.stream(values()).map(Objects::toString).anyMatch(e -> e.equals(enumString));
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static AuditReasonType fromString(String textValue) {
		return stringToEnum.get(textValue);
	}

	AuditReasonType(String textValue, AuditGroupInterface ... pList) {
		this.textValue = textValue;
		for (AuditGroupInterface group : pList) {
			group.addMember(this);
		}
	}

	public boolean is(AuditGroupInterface with) {
		for (AuditReasonType eT : with.getMembers()) {
			if (eT.equals(this)) {
				return true;
			}
		}
		return false;
	}

	public static List<AuditReasonType> getMembersForGroup(AuditReasonTarget auditReasonTarget) {
		List<AuditReasonType> auditReasonTypes = new ArrayList<>();
		for (AuditReasonType auditReasonType : AuditReasonType.values()) {
			if (auditReasonType.is(auditReasonTarget)) {
				auditReasonTypes.add(auditReasonType);
			}
		}
		return auditReasonTypes;
	}

	private interface AuditGroupInterface {

		EnumSet<AuditReasonType> getMembers();

		void addMember(AuditReasonType pE);
	}

	public enum AuditReasonTarget implements AuditGroupInterface {
		UPDATE_STUDY,
		DELETE_STUDY,
		UPDATE_SUBJECT,
		DELETE_SUBJECT;

		private List<AuditReasonType> members = new LinkedList<>();

		@Override
		public EnumSet<AuditReasonType> getMembers() {
			return EnumSet.copyOf(members);
		}

		@Override
		public void addMember(AuditReasonType auditReasonType) {
			members.add(auditReasonType);
		}

		static { // forcing initiation of dependent enum
			try {
				Class.forName(AuditReasonType.class.getName());
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException("Class AuditReasonType not found", ex);
			}
		}
	}
}
