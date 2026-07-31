package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SiteDTOValidator extends AbstractValidator {

    private final NamesDTOValidator namesDTOValidator;

    public SiteDTOValidator(final MessageService messageService, final NamesDTOValidator namesDTOValidator) {
        super(messageService);
	    this.namesDTOValidator = namesDTOValidator;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return SiteDTO.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SiteDTO site = (SiteDTO) o;

        namesDTOValidator.validate(site, errors);

        // Seed
        if (site.getSeed() == null) {
        	errors.rejectValue("seed", "errormessage", getMsg("validator.general.mustNotBeNull"));
        }

        // Study size
        if (site.getCapacity() == null) {
            errors.rejectValue("capacity", "errormessage", getMsg("validator.general.mustNotBeNull"));
        } else if (site.getCapacity() == 0) {
            errors.rejectValue("capacity", "errormessage", getMsg("validator.general.mustNotBeZero"));
        } else if (site.getCapacity() < 2) {
            errors.rejectValue("capacity", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 2));
        }

        // Pseudonym regex
        String regex = site.getPseudonymRegex();
        if (regex == null || regex.trim().isEmpty()) {
        	errors.rejectValue("pseudonymRegex", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
        } else {
            try {
                Pattern.compile(regex);
            } catch(PatternSyntaxException exception){
            	errors.rejectValue("pseudonymRegex", "errormessage", getMsg("validator.regex.invalid"));
            }
        }

    }
}
