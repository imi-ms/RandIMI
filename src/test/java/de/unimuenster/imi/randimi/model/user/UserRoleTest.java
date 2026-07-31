package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class UserRoleTest {

	private static final Random random = new Random();
	private UserRole userRole;

	public UserRoleTest() {
	}

	@BeforeEach
	public void beforeTest() {
		userRole = new UserRole();
	}

	@Test
	public void testGetAndSetUser() {
		RandimiUser testUser = UserTest.getValidUser(false);
		userRole.setUser(testUser);
		assertEquals(testUser, userRole.getUser(), "The user returned was not the one expected.");
		assertTrue(testUser.getUserRoles().contains(userRole), "The user role was not added to the user.");
	}
	
	@Test
	public void testEnumRole() {
		UserRoles testUserRole = Helper.getRandomEnum(UserRoles.class);
		userRole.setEnumRole(testUserRole);
		assertEquals(testUserRole, userRole.getEnumRole(), "The enum user role returned was not the one expected.");
	}
	
	public static UserRole getValidUserRole(boolean withUser) {
		UserRole validUserRole = new UserRole();
		
		validUserRole.setEnumRole(Helper.getRandomEnum(UserRoles.class));
		if(withUser) {
			validUserRole.setUser(UserTest.getValidUser(false));
		}
		
		return validUserRole;
	}
}
