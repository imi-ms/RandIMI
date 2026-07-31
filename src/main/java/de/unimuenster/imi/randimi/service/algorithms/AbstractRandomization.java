package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.validation.Errors;

/**
 * @author Daniel Preciado-Marquez
 */
public abstract class AbstractRandomization extends AbstractValidator implements Randomization {

	public AbstractRandomization(final MessageService messageService) {
		super(messageService);
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return StudyDTO.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		onStudyDTOValidation(errors, (StudyDTO) target);
	}

}
