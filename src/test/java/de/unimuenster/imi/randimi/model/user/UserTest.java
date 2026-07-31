package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class UserTest {

	private static final Random random = new Random();
	private RandimiUser user;

	public UserTest() {
	}

	@BeforeEach
	public void beforeTest() {
		user = new RandimiUser();
	}

	@Test
	public void testGetAndSetUsername() {
		String testUsername = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
		user.setUsername(testUsername);
		assertEquals(testUsername.toUpperCase(), user.getUsername(),
		             "The user name returned was not the one expected.");
	}

	@Test
	public void testGetAndSetPassword() {
		String testPassword = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
		user.setPassword(testPassword);
		assertTrue(new BCryptPasswordEncoder().matches(testPassword, user.getPassword()),
		           "The password returned was not the one expected.");
	}

	@Test
	public void testGetAndSetEnabled() {
		boolean testEnabled = random.nextBoolean();
		user.setEnabled(testEnabled);
		assertEquals(testEnabled, user.isEnabled(), "The enabled value returned was not the one expected.");
	}

	@Test
	public void testGetAndSetFirstName() {
		String testFirstName = Helper.getRandomAlphabeticString(random.nextInt(50) + 3);
		user.setFirstName(testFirstName);
		assertEquals(testFirstName, user.getFirstName(), "The first name returned was not the one expected.");
	}
	
	@Test
	public void testGetAndSetLastName() {
		String testLastName = Helper.getRandomAlphabeticString(random.nextInt(50) + 3);
		user.setLastName(testLastName);
		assertEquals(testLastName, user.getLastName(), "The last name returned was not the one expected.");
	}

	@Test
	public void testGetAndSetEmail() {
		String testEmail = Helper.getRandomMailAddress();
		user.setEMail(testEmail);
		assertEquals(testEmail, user.getEMail(), "The mail address returned was not the one expected.");
	}

	@Test
	public void testGetSetAndRemoveUserRoles() {
		List<UserRole> testUserRoles = new ArrayList<>();
		int counter = random.nextInt(10) + 2;
		for (int i = 0; i < counter; i++) {
			UserRole testUserRole = UserRoleTest.getValidUserRole(false);
			testUserRoles.add(testUserRole);
			user.addUserRole(testUserRole);
			assertTrue(user.getUserRoles().contains(testUserRole), "The user role was not added correctly.");
			assertEquals(user, testUserRole.getUser(), "The user was not set correctly to the user role.");
		}
		assertTrue(user.getUserRoles().containsAll(testUserRoles),
		           "The user roles returned were not the ones expected.");
		for (UserRole userRole : testUserRoles) {
			user.removeUserRole(userRole);
			assertFalse(user.getUserRoles().contains(userRole),"The user role was not removed correctly.");
		}
		assertTrue(user.getUserRoles().isEmpty(), "After removing all added user roles the list was not empty.");
	}

	public static RandimiUser getValidUser(boolean withUserRoles) {
		RandimiUser validUser = new RandimiUser();

		validUser.setUsername(Helper.getRandomAlphanumericString(random.nextInt(50) + 3));
		validUser.setPassword(Helper.getRandomAlphanumericString(random.nextInt(50) + 3));
		validUser.setEnabled(random.nextBoolean());
		validUser.setFirstName(Helper.getRandomAlphabeticString(random.nextInt(50) + 3));
		validUser.setLastName(Helper.getRandomAlphabeticString(random.nextInt(50) + 3));
		validUser.setEMail(Helper.getRandomMailAddress());
		if (withUserRoles) {
			// TODO: implement me!
		}

		return validUser;
	}
}
