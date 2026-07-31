package de.unimuenster.imi.randimi.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Low-level permissions.
 * 
 * @author Tobias Hardt
 */
public enum PermissionType {
	READ_SUBJECT(0),
	CREATE_SUBJECT(1),
	UPDATE_SUBJECT(2),
	DELETE_SUBJECT(3),
	READ_STUDY(4),
	UPDATE_STUDY(5),
	DELETE_STUDY(6),
	READ_AUDIT_SIMPLE(7),
	READ_AUDIT_COMPLEX(8),
	GET_NOTIFICATION(9),
	READ_REPORT(10),
	UPDATE_STUDY_USERS(11);
	
	private final int value;
	private static final Map<Integer, PermissionType> intToEnum = new HashMap<>();

	static // Initialize map from constant value to enum constant
	{
		for (PermissionType cValue : values()) {
			intToEnum.put(cValue.getValue(), cValue);
		}
	}

	PermissionType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
