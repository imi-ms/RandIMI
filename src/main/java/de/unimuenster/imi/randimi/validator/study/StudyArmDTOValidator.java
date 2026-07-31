package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class StudyArmDTOValidator extends AbstractValidator {

	private final NamesDTOValidator namesDTOValidator;

	@Autowired
	public StudyArmDTOValidator(MessageService messageService, NamesDTOValidator namesDTOValidator) {
		super(messageService);
		this.namesDTOValidator = namesDTOValidator;
	}

	@Override
	public boolean supports(Class<?> type) {
		return StudyArmDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		StudyArmDTO studyArmDTO = (StudyArmDTO) o;

		namesDTOValidator.validate(studyArmDTO, errors);

		// Validate Ratio
		Integer ratio = studyArmDTO.getRatio();
		if (ratio == null) {
			errors.rejectValue("ratio", "errormessage", getMsg("validator.general.mustNotBeNull"));
		} else if (ratio <= 0) {
			errors.rejectValue("ratio", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 1));
		}
	}
}
