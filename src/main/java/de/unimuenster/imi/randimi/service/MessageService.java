package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Service
public class MessageService {

	public static final String ERROR_KEY = "error";
	public static final String SUCCESS_KEY = "success";

	private final MessageSource messageSource;

	@Autowired
	public MessageService(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * Checks if the given study may not be modified due to the status of the study.
	 * @param forbidden List of forbidden study status.
	 * @param study The study to be checked.
	 * @param redirectAttributes The RedirectAttributes to add the error to.
	 * @return If the study may not be modified.
	 */
	public boolean checkStudyStatus(final List<StudyStatus> forbidden, final Study study,
	                                 final RedirectAttributes redirectAttributes) {
		if (!forbidden.contains(study.getStatus())) {
			return false;
		}

		if (study.getStatus() == StudyStatus.ARCHIVED) {
			addError(redirectAttributes, "study.error.studyArchived", study.getId());
		}
		if (study.getStatus() == StudyStatus.DELETED) {
			addError(redirectAttributes, "study.error.studyDeleted", study.getId());
		}

		return true;
	}


	public void addError(final RedirectAttributes redirectAttributes, final String messageCode,
	                     final Object... messageArguments) {
		redirectAttributes.addFlashAttribute(ERROR_KEY, getMessage(messageCode, messageArguments));
	}

	public void addError(final RedirectAttributes redirectAttributes, final RandimiException randimiException) {
		redirectAttributes.addFlashAttribute(ERROR_KEY, randimiException.getLocalizedMessage());
	}

	/**
	 * Adds all the errors contained in the given BindingResult to the error attribute.
	 * @param redirectAttributes The RedirectAttributs of the controller method.
	 * @param baseMessage Message at the start of the error message.
	 * @param bindingResult BindingResult containing validation errors.
	 */
	public void addErrors(final RedirectAttributes redirectAttributes, final String baseMessage,
	                      final BindingResult bindingResult) {
		String errorString = getMessage(baseMessage);
		errorString += " " + bindingResult.getAllErrors().get(0).getDefaultMessage();
		for (int i = 1; i < bindingResult.getAllErrors().size(); i++)
			errorString += ", " + bindingResult.getAllErrors().get(i).getDefaultMessage();

		redirectAttributes.addFlashAttribute(ERROR_KEY, errorString);
	}

	public void addSuccess(final RedirectAttributes redirectAttributes, final String messageCode,
	                       final Object... messageArguments) {
		redirectAttributes.addFlashAttribute(SUCCESS_KEY, getMessage(messageCode, messageArguments));
	}

	public String getMessage(final String messageCode) {
		return getMessage(messageCode, getLocale());
	}

	public String getMessage(final String messageCode, Locale locale) {
		return messageSource.getMessage(messageCode, null, locale);
	}

	public String getMessage(final String messageCode, final Object... objects) {
		return messageSource.getMessage(messageCode, objects, getLocale());
	}

	private Locale getLocale() {
		return LocaleContextHolder.getLocale();
	}
}
