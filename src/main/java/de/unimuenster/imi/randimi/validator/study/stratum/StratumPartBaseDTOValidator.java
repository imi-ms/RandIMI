package de.unimuenster.imi.randimi.validator.study.stratum;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import de.unimuenster.imi.randimi.validator.study.NamesDTOValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paul Schaub
 */
@Component
public class StratumPartBaseDTOValidator extends AbstractValidator {

    private final NamesDTOValidator namesDTOValidator;

    public StratumPartBaseDTOValidator(MessageService messageService, NamesDTOValidator namesDTOValidator) {
        super(messageService);
	    this.namesDTOValidator = namesDTOValidator;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return StratumPartBaseDTO.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        StratumPartBaseDTO dto = (StratumPartBaseDTO) o;

        if (!isEnumStratum(dto)) {
            validateIntervalStratumPart(errors, dto);
        } else {
            validateEnumStratumPart(errors, dto);
        }
    }

    private void validateEnumStratumPart(Errors errors, StratumPartBaseDTO dto) {
        namesDTOValidator.validate(dto, errors);
    }

    private void validateIntervalStratumPart(Errors errors, StratumPartBaseDTO dto) {
        boolean hasNull = false;
        if (dto.getIntervalBegin() == null) {
            errors.rejectValue("intervalBegin", "errormessage", getMsg("validator.general.mustNotBeNull"));
            hasNull = true;
        }
        if (dto.getIntervalEnd() == null) {
            errors.rejectValue("intervalEnd", "errormessage", getMsg("validator.general.mustNotBeNull"));
            hasNull = true;
        }
        if (!hasNull && dto.getIntervalBegin() > dto.getIntervalEnd()) {
            errors.rejectValue("intervalBegin", "errormessage", "Interval start must not be bigger than interval end.");
        }
    }

    public boolean isEnumStratum(StratumPartBaseDTO dto) {
        return dto.getGuiName() != null;
    }

    public void validateNoIntervalIntersections(Errors errors, List<StratumPartBaseDTO> parts) {
        List<Float[]> intervals = new ArrayList<>();
        for (int i = 0, partsSize = parts.size(); i < partsSize; i++) {
            StratumPartBaseDTO part = parts.get(i);
            if (part.getIntervalBegin() == null || part.getIntervalEnd() == null) {
                continue;
            }

            errors.pushNestedPath("stratumParts[" + i + "]");
            for (Float[] currentInterval : intervals) {
                if (isInRange(part.getIntervalBegin(), currentInterval[0], currentInterval[1])
                        || isInRange(part.getIntervalEnd(), currentInterval[0], currentInterval[1])) {
                    errors.rejectValue("intervalBegin", "errormessage", getMsg("validator.study.stratumValueInOtherRange"));
                } else if (isInRange(currentInterval[0], part.getIntervalBegin(), part.getIntervalEnd())
                        || isInRange(currentInterval[1], part.getIntervalBegin(), part.getIntervalEnd())) {
                    errors.rejectValue("intervalBegin", "errormessage", getMsg("validator.study.stratumValueInOtherRange"));
                }
            }
            errors.popNestedPath();
            intervals.add(new Float[]{part.getIntervalBegin(), part.getIntervalEnd()});
        }
    }

    private boolean isInRange(Float number, Float min, Float max) {
        return max.compareTo(number) >= 0 && min.compareTo(number) <= 0;
    }
}
