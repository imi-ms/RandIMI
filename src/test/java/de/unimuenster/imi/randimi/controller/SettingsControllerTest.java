package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.settings.SettingsDTO;
import de.unimuenster.imi.randimi.mapping.settings.SettingsMapper;
import de.unimuenster.imi.randimi.model.settings.Settings;
import de.unimuenster.imi.randimi.repository.settings.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

public class SettingsControllerTest extends MVCControllerTestBase {

	@Autowired
	SettingsRepository settingsRepository;

	@Autowired
	SettingsMapper settingsMapper;

	@Test
	public void editMainColor() throws Exception {
		String newMainColor = "#123456";

		Settings settings = settingsRepository.getCurrentSettings();
		settings.setMainColor(newMainColor);
		SettingsDTO dto = settingsMapper.toSettingsDTO(settings);

		mockMvc.perform((post("/settings/edit"))
				                .with(csrf())
				                .contentType(MediaType.APPLICATION_JSON)
				                .param("action", "save")
				                .flashAttr(SettingsController.FORM_MODEL_KEY, dto))
		       .andDo(print())
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/settings/edit"))
		       .andExpect(flash().attributeExists("success"));

		settings = settingsRepository.getCurrentSettings();
		assertEquals(newMainColor, settings.getMainColor());
	}

}
