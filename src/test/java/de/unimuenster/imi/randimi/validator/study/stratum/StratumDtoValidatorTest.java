package de.unimuenster.imi.randimi.validator.study.stratum;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.*;

import static org.mockito.Mockito.*;

public class StratumDtoValidatorTest extends ValidatorTestBase {

    @Autowired
    StratumDTOValidator validator;

    @Test
    public void validateValidEnumDto() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.ENUM);

        validator.validate(dto, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    public void validateValidIntervalDto() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.INTERVAL);

        validator.validate(dto, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    private StratumDTO getValidDto() {
        Random random = new Random();
        return getValidDto(StratumType.values()[random.nextInt(StratumType.values().length)]);
    }

    public static StratumDTO getValidDto(StratumType type) {
        StratumDTO dto = new StratumDTO();
        dto.setId(5L);
        dto.setStudyId(13L);
        dto.setGuiName("DayOfWeek");
        dto.setApiId("DayOfWeek");
        dto.setUseApiId(false);
        dto.setStratumType(type);
        dto.setOrderNumber(1);

        List<StratumPartBaseDTO> stratumParts = new ArrayList<>();
        switch (type) {
            case ENUM:
                for (String s : Arrays.asList("Monday", "Tuesday", "My Dudes", "Thursday", "Friday", "Saturday", "Sunday")) {
                    stratumParts.add(getValidEnumPartDto(s));
                }
                break;
            case INTERVAL:
                StratumPartBaseDTO inter1 = new StratumPartBaseDTO();
                inter1.setIntervalBegin(0f);
                inter1.setIntervalEnd(2f);
                stratumParts.add(inter1);
                StratumPartBaseDTO inter2 = new StratumPartBaseDTO();
                inter2.setIntervalBegin(3f);
                inter2.setIntervalEnd(5f);
                stratumParts.add(inter2);
                break;
        }
        dto.setStratumParts(stratumParts);
        return dto;
    }

    public static StratumPartBaseDTO getValidEnumPartDto(final String name) {
        StratumPartBaseDTO enumPart = new StratumPartBaseDTO();
        enumPart.setEnumValue(name);
        enumPart.setApiId(name);
        enumPart.setUseApiId(false);
        return enumPart;
    }

    @Test
    public void validateNamesValidator() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto();
        dto.setGuiName(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
    }

    @Test
    public void validateStratumTypeNull() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto();
        dto.setGuiName("foo");
        dto.setStratumType(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("stratumParts", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateEnumStratumNoParts() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.ENUM);

        dto.setStratumParts(Collections.emptyList());

        validator.validate(dto, errors);

        verify(errors).rejectValue("stratumParts", "errormessage", getMsg("validator.stratum.tooLessValues"));
    }

    @Test
    public void validateEnumStratumTooFewParts() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.ENUM);

        StratumPartBaseDTO part = getValidEnumPartDto("OnlyOne");
        dto.setStratumParts(Collections.singletonList(part));

        validator.validate(dto, errors);

        verify(errors).rejectValue("stratumParts", "errormessage", getMsg("validator.stratum.tooLessValues"));
    }

    @Test
    public void validateEnumPartValueEmpty() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.ENUM);

        dto.getStratumParts().get(0).setEnumValue(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
    }

    @Test
    public void validateIntervalStratumNoParts() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.INTERVAL);
        dto.setStratumParts(Collections.emptyList());

        validator.validate(dto, errors);

        verify(errors).rejectValue("stratumParts", "errormessage", getMsg("validator.stratum.tooLessIntervals"));
    }

    @Test
    public void validateIntervalStratumTooFewParts() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.INTERVAL);
        StratumPartBaseDTO part = new StratumPartBaseDTO();
        part.setIntervalBegin(1f);
        part.setIntervalEnd(2f);
        dto.setStratumParts(Collections.singletonList(part));

        validator.validate(dto, errors);

        verify(errors).rejectValue("stratumParts", "errormessage", getMsg("validator.stratum.tooLessIntervals"));
    }

    @Test
    public void validateIntervalBeginNull() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.INTERVAL);

        StratumPartBaseDTO valid = new StratumPartBaseDTO();
        valid.setIntervalBegin(0f);
        valid.setIntervalEnd(0.5f);

        StratumPartBaseDTO invalid = new StratumPartBaseDTO();
        invalid.setIntervalBegin(null);
        invalid.setIntervalEnd(2f);

        dto.setStratumParts(Arrays.asList(valid, invalid));

        validator.validate(dto, errors);

        verify(errors).rejectValue("intervalBegin", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateIntervalEndNull() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.INTERVAL);

        StratumPartBaseDTO valid = new StratumPartBaseDTO();
        valid.setIntervalBegin(0f);
        valid.setIntervalEnd(0.5f);

        StratumPartBaseDTO invalid = new StratumPartBaseDTO();
        invalid.setIntervalBegin(1f);
        invalid.setIntervalEnd(null);

        dto.setStratumParts(Arrays.asList(valid, invalid));

        validator.validate(dto, errors);

        verify(errors).rejectValue("intervalEnd", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateOverlappingIntervals() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.INTERVAL);

        StratumPartBaseDTO valid = new StratumPartBaseDTO();
        valid.setIntervalBegin(0f);
        valid.setIntervalEnd(2f);

        StratumPartBaseDTO invalid = new StratumPartBaseDTO();
        invalid.setIntervalBegin(1f);
        invalid.setIntervalEnd(3f);

        dto.setStratumParts(Arrays.asList(valid, invalid));

        validator.validate(dto, errors);

        verify(errors).rejectValue("intervalBegin", "errormessage", getMsg("validator.study.stratumValueInOtherRange"));
    }

    @Test
    public void testIntervalEndBeforeBegin() {
        Errors errors = mock(Errors.class);
        StratumDTO dto = getValidDto(StratumType.INTERVAL);

        StratumPartBaseDTO valid = new StratumPartBaseDTO();
        valid.setIntervalBegin(2f);
        valid.setIntervalEnd(1f);

        StratumPartBaseDTO invalid = new StratumPartBaseDTO();
        invalid.setIntervalBegin(3f);
        invalid.setIntervalEnd(4f);

        dto.setStratumParts(Arrays.asList(valid, invalid));

        validator.validate(dto, errors);

        verify(errors).rejectValue("intervalBegin", "errormessage", "Interval start must not be bigger than interval end.");
    }
}
