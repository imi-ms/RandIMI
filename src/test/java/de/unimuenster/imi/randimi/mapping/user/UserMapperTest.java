package de.unimuenster.imi.randimi.mapping.user;

import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

	private UserMapper userMapper;

	@BeforeEach
	public void setUp() {
		userMapper = new UserMapper();
	}

	@Test
	public void applyRolesAdmin() {
		// Preparation
		final RandimiUser admin = new RandimiUser();
		admin.addUserRole(new UserRole(UserRoles.ROLE_ADMIN));

		final RandimiUser user = new RandimiUser();
		user.addUserRole(new UserRole(UserRoles.ROLE_LOCAL_MANAGER));

		final UserDTO userDTO = new UserDTO();
		userDTO.setUserRoles(List.of(UserRoles.ROLE_ADMIN.name(), UserRoles.ROLE_API_USER.name()));

		// Test
		userMapper.applyRoles(userDTO, user, admin);

		final List<UserRoles> expectedRoles = List.of(UserRoles.ROLE_ADMIN, UserRoles.ROLE_API_USER);
		final List<UserRoles> actualRoles = user.getUserRoles().stream().map(UserRole::getEnumRole).toList();
		assertEquals(expectedRoles, actualRoles, "The user roles were not applied correctly!");
	}

	@Test
	public void applyRolesLocalManagerAdd() {
		// Preparation
		final RandimiUser localManager = new RandimiUser();
		localManager.addUserRole(new UserRole(UserRoles.ROLE_LOCAL_MANAGER));

		final RandimiUser user = new RandimiUser();
		user.addUserRole(new UserRole(UserRoles.ROLE_API_USER));

		final UserDTO userDTO = new UserDTO();
		userDTO.setUserRoles(List.of(UserRoles.ROLE_ADMIN.name(), UserRoles.ROLE_LOCAL_MANAGER.name()));

		// Test
		userMapper.applyRoles(userDTO, user, localManager);

		final List<UserRoles> expectedRoles = List.of(UserRoles.ROLE_API_USER, UserRoles.ROLE_LOCAL_MANAGER);
		final List<UserRoles> actualRoles = user.getUserRoles().stream().map(UserRole::getEnumRole).toList();
		assertEquals(expectedRoles, actualRoles, "The user roles were not applied correctly!");
	}

	@Test
	public void applyRolesLocalManagerRemove() {
		// Preparation
		final RandimiUser localManager = new RandimiUser();
		localManager.addUserRole(new UserRole(UserRoles.ROLE_LOCAL_MANAGER));

		final RandimiUser user = new RandimiUser();
		user.addUserRole(new UserRole(UserRoles.ROLE_LOCAL_MANAGER));

		final UserDTO userDTO = new UserDTO();
		userDTO.setUserRoles(List.of(UserRoles.ROLE_ADMIN.name()));

		// Test
		userMapper.applyRoles(userDTO, user, localManager);

		final List<UserRoles> expectedRoles = List.of();
		final List<UserRoles> actualRoles = user.getUserRoles().stream().map(UserRole::getEnumRole).toList();
		assertEquals(expectedRoles, actualRoles, "The user roles were not applied correctly!");
	}

}
