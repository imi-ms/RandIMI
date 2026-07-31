package de.unimuenster.imi.randimi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.unimuenster.imi.randimi.FieldErrorMatcher;
import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

public class UserControllerTest extends MVCControllerTestBase {

	@Test
	public void inviteUserCancelTest() throws Exception {
		final UserDTO userDTO = getValidUserDTO(false);

		mockMvc.perform(post("/users/invite")
				                .with(csrf())
				                .contentType(MediaType.APPLICATION_JSON)
				                .param("action", "cancel")
				                .flashAttr("user", userDTO))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/users"));
	}

	@Test
	public void inviteUserSkipEMailValidationTest() throws Exception {
		final UserDTO userDTO = getValidUserDTO(true);

		mockMvc.perform(post("/users/invite")
				                .with(csrf())
				                .contentType(MediaType.APPLICATION_JSON)
				                .param("action", "save")
				                .flashAttr("user", userDTO))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/users"))
		       .andExpect(flash().attributeExists("success"));

		RandimiUser user = randimiUserRepository.findFirstByUsernameIgnoreCase("username");
		assertNotNull(user, "User not found!");
		assertTrue(user.isEnabled(), "User not enabled!");
	}

	@Test
	public void saveUserChanges() throws Exception {
		final AccountDetailsDTO dto = getValidAccountDetailsDTOWithPasswordChange();

		RandimiUser user = randimiUserRepository.findById(dto.getId()).orElseThrow();
		String originalPassword = user.getPassword();

		mockMvc.perform((post("/users/edit"))
				                .with(csrf())
				                .contentType(MediaType.APPLICATION_JSON)
				                .param("action", "save")
				                .flashAttr("accountDetails", dto))
		       .andDo(print())
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/users/edit"))
		       .andExpect(flash().attributeExists("success"));

		RandimiUser updatedUser = randimiUserRepository.findById(dto.getId()).orElseThrow();
		assertNotEquals(originalPassword, updatedUser.getPassword(), "Password should have changed!");
	}

	@Test
	public void saveUserChangesWrongPassword() throws Exception {
		final AccountDetailsDTO dto = getValidAccountDetailsDTOWithPasswordChange();
		dto.setOldPassword("wrong");

		final Map<String, String> expectedFieldErrors = new HashMap<>();
		expectedFieldErrors.put("oldPassword", messageService.getMessage("validator.user.oldPasswordMismatch"));

		doSaveUserChanges(dto, expectedFieldErrors);
	}

	@Test
	public void saveUserChangesWeakPassword() throws Exception {
		final AccountDetailsDTO dto = getValidAccountDetailsDTOWithPasswordChange();
		dto.setNewPassword("changed");
		dto.setRepeatPassword("changed");

		final Map<String, String> expectedFieldErrors = new HashMap<>();
		expectedFieldErrors.put("newPassword", messageService.getMessage("validator.user.password.mustBeLongerThan", 8));

		doSaveUserChanges(dto, expectedFieldErrors);
	}

	@Test
	public void saveUserChangesPasswordMismatch() throws Exception {
		final AccountDetailsDTO dto = getValidAccountDetailsDTOWithPasswordChange();
		dto.setNewPassword("changedPassword");
		dto.setRepeatPassword("changedOtherPassword");

		final Map<String, String> expectedFieldErrors = new HashMap<>();
		expectedFieldErrors.put("repeatPassword", messageService.getMessage("validator.user.passwordMismatch"));

		doSaveUserChanges(dto, expectedFieldErrors);
	}

	@Test
	public void delete() throws Exception {
		RandimiUser activeUser = getActiveUser();

		mockMvc.perform((post("/users/delete"))
				                .with(csrf())
				                .contentType(MediaType.APPLICATION_JSON)
				                .param("userId", String.valueOf(activeUser.getId())))
		       .andDo(print())
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/users"))
		       .andExpect(flash().attributeExists("success"));

		assertFalse(randimiUserRepository.existsById(activeUser.getId()), "User should have been deleted!");
	}

	private void doSaveUserChanges(final AccountDetailsDTO dto,
	                               final Map<String, String> expectedFieldErrors) throws Exception {
		RandimiUser user = randimiUserRepository.findById(dto.getId()).orElseThrow();
		String originalPassword = user.getPassword();

		mockMvc.perform((post("/users/edit"))
				                .with(csrf())
				                .contentType(MediaType.APPLICATION_JSON)
				                .param("action", "save")
				                .flashAttr("accountDetails", dto))
		       .andDo(print())
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/users/edit"))
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.accountDetails"))
		       .andExpect(flash().attribute("org.springframework.validation.BindingResult.accountDetails",
		                                    new FieldErrorMatcher(expectedFieldErrors)));

		RandimiUser updatedUser = randimiUserRepository.findById(dto.getId()).orElseThrow();
		assertEquals(originalPassword, updatedUser.getPassword(), "Password should not have changed!");
	}
}
