package de.unimuenster.imi.randimi.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Roles used for authentication.
 *
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
public enum UserRoles {
	ROLE_API_USER("ROLE_API_USER"),
	ROLE_STUDY_MANAGER("ROLE_STUDY_MANAGER"),
	ROLE_USER_MANAGER("ROLE_USER_MANAGER"),
	ROLE_LOCAL_MANAGER("ROLE_LOCAL_MANAGER"),
	ROLE_ADMIN("ROLE_ADMIN");
	private String textValue;
	private static final Map<String, UserRoles> stringToEnum = new HashMap<>();

	static // Initialize map from constant name to enum constant
	{
		for (UserRoles cValue : values()) {
			stringToEnum.put(cValue.toString(), cValue);
		}
	}

	UserRoles(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static UserRoles fromString(String textValue) {
		return stringToEnum.get(textValue);
	}
}
