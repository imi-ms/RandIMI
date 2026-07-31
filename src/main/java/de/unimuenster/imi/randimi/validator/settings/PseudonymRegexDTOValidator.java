package de.unimuenster.imi.randimi.validator.settings;

import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDTO;
import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDescriptionDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class PseudonymRegexDTOValidator extends AbstractValidator {

	final private PseudonymRegexDescriptionDTOValidator pseudonymRegexDescriptionDTOValidator;

	@Autowired
	public PseudonymRegexDTOValidator(final MessageService messageService,
	                                  final PseudonymRegexDescriptionDTOValidator pseudonymRegexDescriptionDTOValidator) {
		super(messageService);
		this.pseudonymRegexDescriptionDTOValidator = pseudonymRegexDescriptionDTOValidator;
	}

	@Override
	public boolean supports(Class<?> type) {
		return PseudonymRegexDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		PseudonymRegexDTO pseudonymRegexDTO = (PseudonymRegexDTO) o;

		if (pseudonymRegexDTO.getOrderNumber() == null)
			errors.rejectValue("orderNumber", "errormessage", getMsg("validator.general.mustNotBeEmpty"));

		if (pseudonymRegexDTO.getOrderNumber() < 0)
			errors.rejectValue("orderNumber", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 0));

		final List<PseudonymRegexDescriptionDTO> pseudonymRegexDescriptionDTOList = pseudonymRegexDTO.getPseudonymRegexDescriptionDTOList();
		for (int i = 0; i < pseudonymRegexDescriptionDTOList.size(); ++i) {
			errors.pushNestedPath("pseudonymRegexDescriptionDTOList[" + i + "]");
			pseudonymRegexDescriptionDTOValidator.validate(pseudonymRegexDescriptionDTOList.get(i), errors);
			errors.popNestedPath();
		}

		if (pseudonymRegexDTO.getRegex() == null || pseudonymRegexDTO.getRegex().trim().isEmpty()) {
			errors.rejectValue("regex", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else if (pseudonymRegexDTO.getRegex().length() > 255) {
			errors.rejectValue("regex", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
		} else {
			try {
				Pattern.compile(pseudonymRegexDTO.getRegex());
			} catch(PatternSyntaxException exception){
				errors.rejectValue("regex", "errormessage", getMsg("validator.regex.invalid"));
			}
		}
	}
}
