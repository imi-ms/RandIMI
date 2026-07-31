package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDTO;
import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDescriptionDTO;
import de.unimuenster.imi.randimi.dto.settings.SettingsDTO;
import de.unimuenster.imi.randimi.mapping.settings.SettingsMapper;
import de.unimuenster.imi.randimi.model.enumeration.GravatarDefaultOption;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.settings.Settings;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandimiMailService;
import de.unimuenster.imi.randimi.repository.settings.SettingsRepository;
import de.unimuenster.imi.randimi.validator.settings.SettingsDTOValidator;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.mail.AuthenticationFailedException;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Class to handle all setting related requests.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */

@Hidden
@Controller
@RequestMapping(value = "/settings")
public class SettingsController {
	/* Logger used for logging... doh. */
	private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);

	/* Magic number */
	public static final String FORM_MODEL_KEY = "settings";

	/* DAO to access and persist the server settings. */
	private final SettingsRepository settingsRepository;

	private final SettingsMapper settingsMapper;

	/* Validator used for the validartion of the DTO object. */
	private final SettingsDTOValidator settingsDTOValidator;

	/* Used to generate localized error messages. */
	private final MessageService messageService;
	/* Bean needed to update properties of mail service at runtime. */
	private final RandimiMailService randimiMailService;

	public SettingsController(final SettingsRepository settingsRepository, final SettingsMapper settingsMapper,
	                          final SettingsDTOValidator settingsDTOValidator, final MessageService messageService,
	                          final RandimiMailService randimiMailService) {
		this.settingsRepository = settingsRepository;
		this.settingsMapper = settingsMapper;
		this.settingsDTOValidator = settingsDTOValidator;
		this.messageService = messageService;
		this.randimiMailService = randimiMailService;
	}

	//----------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------
	// Request functions
	//----------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------
	
	/**
	 * Method used to display and edit the current server settings.
	 * 
	 * @param model Current Model
	 * @return The "edit" view.
	 */
	@RequestMapping(value ="/edit", method = RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String getSettings(Model model) {
		// Check if it is the first call or a redirect
		SettingsDTO currentSettings = (SettingsDTO) model.asMap().get(FORM_MODEL_KEY);
		if(currentSettings == null) {
			currentSettings= settingsMapper.toSettingsDTO(settingsRepository.getCurrentSettings());
		}

		PseudonymRegexDTO pseudonymRegexTemplate = new PseudonymRegexDTO();
		for (SupportedLanguage language : SupportedLanguage.values()) {
			PseudonymRegexDescriptionDTO descriptionTemplate = new PseudonymRegexDescriptionDTO();
			descriptionTemplate.setCurrentLanguage(language);
			descriptionTemplate.setName(messageService.getMessage("settings.edit.pseudonymTemplates.newTemplate", language.toLocale()));
			pseudonymRegexTemplate.getPseudonymRegexDescriptionDTOList().add(descriptionTemplate);
		}

		model.addAttribute("pseudonymRegexTemplate", pseudonymRegexTemplate);
		model.addAttribute(FORM_MODEL_KEY, currentSettings);
		model.addAttribute("supportedLanguages", SupportedLanguage.values());
		//gravatar
		model.addAttribute("supportedGravatarOptions", GravatarDefaultOption.values());
		model.addAttribute("supportedGravatarLabels", GravatarDefaultOption.getDisplayNames());
		model.addAttribute("supportedGravatarImages", GravatarDefaultOption.getPreviewUrls());
		return "/settings/edit";
	}
	
	/**
	 * Persists the current setting's changes.
	 * 
	 * @param action Return on cancel action.
	 * @param settingsDTO DTO containing the changed settings.
	 * @param result Result of the validation.
	 * @param ra RedirectAttributes used to transfer messages to the redirected page.
	 * @return Redirect to get, or welcome in case of an error.
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String editSettings(@RequestParam String action,
	                           @Valid @ModelAttribute(FORM_MODEL_KEY) SettingsDTO settingsDTO, BindingResult result,
	                           RedirectAttributes ra) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/welcome";
		}
		
		// Validate the settings
		settingsDTOValidator.validate(settingsDTO, result);
		
		if (result.hasErrors()) {
			//pass form and bindung errors
			ra.addFlashAttribute(FORM_MODEL_KEY,settingsDTO);
			ra.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY, result);
			// and error message
			ra.addFlashAttribute("error", messageService.getMessage("general.error.invalidForm"));
			return "redirect:/settings/edit";
		}

		final Settings oldSettings;
		if (settingsDTO.getId() == null || settingsDTO.getId() == 0) {
			oldSettings = new Settings();
		} else {
			Optional<Settings> settingsOptional = settingsRepository.findById(settingsDTO.getId());
			oldSettings = settingsOptional.orElse(new Settings());
		}

		final Settings editedSettings = settingsMapper.toSettings(settingsDTO, oldSettings);

		settingsRepository.save(editedSettings);

		//Update current beans
		updateMailServiceSettings();

		ra.addFlashAttribute("success", messageService.getMessage("general.success.saved"));
		return "redirect:/settings/edit";
	}

	@RequestMapping(value = "/sendMail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ResponseBody
	public Map<String, String> sendMail(@RequestParam final String receiver,
	                                    @RequestParam final String mailHost, @RequestParam final int mailPort,
	                                    @RequestParam final String mailUsername,
	                                    @RequestParam final String mailPassword,
	                                    @RequestParam final boolean mailSMTPAuth, @RequestParam final boolean mailTLS) {
		final Map<String, String> response = new HashMap<>();

		String statusCode = "ok";
		String messageCode = "settings.edit.testMail.success";

		try {
			final String subject = randimiMailService.assembleMailSubject("mail.testMailService.subject");
			final String text = randimiMailService.assembleMailText("mail.testMailService.content");

			randimiMailService.configureMailSender(mailHost, mailPort, mailUsername, mailPassword, mailSMTPAuth, mailTLS);
			randimiMailService.sendSimpleMessageOrThrow(receiver, subject, text);
		} catch (final MailException mailException) {
			statusCode = "error";
			final Throwable cause = mailException.getMostSpecificCause();
			if (cause instanceof AuthenticationFailedException) {
				messageCode = "settings.edit.testMail.error.authenticationFailed";
			} else if (cause instanceof ConnectException) {
				messageCode = "settings.edit.testMail.error.connection";
			} else if (cause instanceof MailSendException) {
				final String msg = cause.getMessage();
				if (msg.contains("450 4.1.1"))
					messageCode = "settings.edit.testMail.error.receiverLocal";
				else if (msg.contains("450 4.1.2"))
					messageCode = "settings.edit.testMail.error.receiverDomain";
				else if (msg.contains("530 5.7.0"))
					messageCode = "settings.edit.testMail.error.SMTPRequiresTLS";
				else if (msg.contains("553 5.7.1"))
					messageCode = "settings.edit.testMail.error.hostRequiresSMTP";
				else {
					messageCode = "settings.edit.testMail.error.unknown";
					LOGGER.error(mailException);
				}
			} else if (cause instanceof UnknownHostException) {
				messageCode = "settings.edit.testMail.error.unknownHost";
			} else {
				messageCode = "settings.edit.testMail.error.unknown";
				LOGGER.error(mailException);
			}
		} finally {
			randimiMailService.configureMailSender();
		}

		response.put("status", statusCode);
		response.put("message", messageService.getMessage(messageCode));

		return response;
	}

	//----------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------
	// Private helper functions
	//----------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------

	/**
	 * Used to update the mail server settings without restarting the application.
	 */
	private void updateMailServiceSettings() {
		randimiMailService.configureMailSender();
	}
}
