package de.unimuenster.imi.randimi.repository.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import de.unimuenster.imi.randimi.model.user.RandimiUser;

import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Test;

/**
 * @author Daniel Preciado-Marquez <daniel.preciado.marquez@uni-muenster.de>
 */
public class RandimiUserRepositoryTest extends RepositoryTestBase {

	@Test
	public void findFirstByUsernameIgnoreCaseTest() {
		RandimiUser admin = randimiUserRepository.findFirstByUsernameIgnoreCase("ADMIN");
		assertNotNull(admin, "User not found!");
		assertEquals("ADMIN", admin.getUsername(), "Found wrong user!");

		// Test user name with different capitalization
		RandimiUser activeUser = randimiUserRepository.findFirstByUsernameIgnoreCase("Active_Test_User");
		assertNotNull(activeUser, "User not found!");
		assertEquals("ACTIVE_TEST_USER", activeUser.getUsername(), "Found wrong user!");

		RandimiUser notExistingUser = randimiUserRepository.findFirstByUsernameIgnoreCase("");
		assertNull(notExistingUser, "Found nonexistent user!");
	}

	@Test
	public void doesUsernameAlreadyExistTest() {
		RandimiUser admin = randimiUserRepository.findFirstByUsernameIgnoreCase("ADMIN");
		RandimiUser activeUser = randimiUserRepository.findFirstByUsernameIgnoreCase("Active_Test_User");

		// Existing user name same id
		boolean exists = randimiUserRepository.doesUsernameAlreadyExist("ADMIN", admin.getId());
		assertFalse(exists);

		// Existing user name different id
		exists = randimiUserRepository.doesUsernameAlreadyExist("ADMIN", activeUser.getId());
		assertTrue(exists);

		// Nonexistent user name
		exists = randimiUserRepository.doesUsernameAlreadyExist("", admin.getId());
		assertFalse(exists);
	}

	@Test
	public void findFirstByInvitationTokenTest() {
		RandimiUser inactiveUser = randimiUserRepository.findFirstByInvitationToken("INVITATION_TOKEN");
		assertNotNull(inactiveUser, "User not found!");
		assertEquals("INACTIVE_TEST_USER", inactiveUser.getUsername(), "Found wrong user!");

		// Token null
		RandimiUser admin = randimiUserRepository.findFirstByInvitationToken(null);
		assertNotNull(admin, "User not found!");
		assertEquals("ADMIN", admin.getUsername(), "Found wrong user!");

		// Token nonexistent
		RandimiUser nonexistentUser = randimiUserRepository.findFirstByInvitationToken("");
		assertNull(nonexistentUser, "Found nonexistent user!");
	}

	@Test
	public void getObsoleteUsersTest() {
		List<RandimiUser> obsoleteUsers = randimiUserRepository.getObsoleteUsers();

		assertEquals(1, obsoleteUsers.size());
		assertTrue(obsoleteUsers.get(0)
		                        .getInvitationTimestamp()
		                        .before(Timestamp.valueOf(LocalDateTime.now().minusDays(30))), "User is not obsolete!");
		assertEquals("OBSOLETE_TEST_USER", obsoleteUsers.get(0).getUsername(), "Found wrong user!");
	}

	@Test
	public void getNotifiedUsersOfStudyTest() {
		List<RandimiUser> notifiedUsers = randimiUserRepository.getNotifiedUsersOfStudy(activeStudy);

		assertEquals(1, notifiedUsers.size());
		assertEquals("ACTIVE_TEST_USER", notifiedUsers.get(0).getUsername(), "Found wrong user!");
	}

}
