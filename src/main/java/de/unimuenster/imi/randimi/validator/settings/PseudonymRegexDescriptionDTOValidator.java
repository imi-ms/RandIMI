package de.unimuenster.imi.randimi.validator.settings;

import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDescriptionDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class PseudonymRegexDescriptionDTOValidator extends AbstractValidator {

    @Autowired
    public PseudonymRegexDescriptionDTOValidator(final MessageService messageService) {
        super(messageService);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return PseudonymRegexDescriptionDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final PseudonymRegexDescriptionDTO dto = (PseudonymRegexDescriptionDTO) target;

        if (dto.getCurrentLanguage() == null)
            errors.rejectValue("currentLanguage", "errormessage", getMsg("validator.general.mustNotBeNull"));

        if (dto.getName() == null || dto.getName().trim().isEmpty())
            errors.rejectValue("name", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
        else if (dto.getName().length() > 255)
            errors.rejectValue("name", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty())
            errors.rejectValue("description", "errormessage", getMsg("validator.general.mustNotBeEmpty"));

    }
}
