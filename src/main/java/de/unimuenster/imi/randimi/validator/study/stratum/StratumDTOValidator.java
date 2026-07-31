package de.unimuenster.imi.randimi.validator.study.stratum;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import de.unimuenster.imi.randimi.validator.study.NamesDTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class StratumDTOValidator extends AbstractValidator {

	private final NamesDTOValidator namesDTOValidator;
	private final StratumPartBaseDTOValidator partValidator;

	@Autowired
	public StratumDTOValidator(MessageService messageService, NamesDTOValidator namesDTOValidator,
	                           StratumPartBaseDTOValidator partValidator) {
		super(messageService);
		this.namesDTOValidator = namesDTOValidator;
		this.partValidator = partValidator;
	}

	@Override
	public boolean supports(Class<?> type) {
		return StratumDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		StratumDTO stratumDTO = (StratumDTO) o;

		namesDTOValidator.validate(stratumDTO, errors);

		if (stratumDTO.getStratumType() == null) {
			errors.rejectValue("stratumParts", "errormessage", getMsg("validator.general.mustNotBeNull"));
		}

		if (stratumDTO.getStratumType() == StratumType.ENUM) {
			// Enum list size

			if (stratumDTO.getStratumParts().size() < 2) {
				errors.rejectValue("stratumParts", "errormessage", getMsg("validator.stratum.tooLessValues"));
			}

			// Stratum parts
			for (int i = 0; i < stratumDTO.getStratumParts().size(); i++) {
				StratumPartBaseDTO part = stratumDTO.getStratumParts().get(i);
				if (!partValidator.isEnumStratum(part)) {
					errors.rejectValue("stratumParts", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
				}
				errors.pushNestedPath("stratumParts[" + i + "]");
				partValidator.validate(part, errors);
				errors.popNestedPath();
			}

			namesDTOValidator.validateNames(stratumDTO.getStratumParts(), errors, "stratumParts");
		} // Interval strata

		if (stratumDTO.getStratumType() == StratumType.INTERVAL) {
			// Interval size
			if (stratumDTO.getStratumParts().size() < 2) {
				errors.rejectValue("stratumParts", "errormessage", getMsg("validator.stratum.tooLessIntervals"));
			}

			// Interval limits
			for (int i = 0; i < stratumDTO.getStratumParts().size(); i++) {
				StratumPartBaseDTO part = stratumDTO.getStratumParts().get(i);
				errors.pushNestedPath("stratumParts[" + i + "]");
				partValidator.validate(part, errors);

				// Check strata intervals
				errors.popNestedPath();
			}

			partValidator.validateNoIntervalIntersections(errors, stratumDTO.getStratumParts());
		}
	}
}
