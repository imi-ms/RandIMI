package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.NamesDTO;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class NamesDtoValidatorTest extends ValidatorTestBase {

	@Autowired private NamesDTOValidator validator;

	@Test
	public void validateValidNamesDto() {
		Errors errors = mock(Errors.class);
		NamesDTO namesDto = getValidNamesDTO();

		validator.validate(namesDto, errors);

		verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
	}

	//=============
	//-- GuiName --
	//=============

	@Test
	public void validateGuiNameNull() {
		Errors errors = mock(Errors.class);
		NamesDTO namesDto = getValidNamesDTO();
		namesDto.setGuiName(null);

		validator.validate(namesDto, errors);

		verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
	}

	@Test
	public void validateGuiNameEmpty() {
		Errors errors = mock(Errors.class);
		NamesDTO namesDto = getValidNamesDTO();
		namesDto.setGuiName(" ");

		validator.validate(namesDto, errors);

		verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
	}

	@Test
	public void validateGuiNameTooLong() {
		Errors errors = mock(Errors.class);
		NamesDTO namesDto = getValidNamesDTO();
		namesDto.setGuiName("loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong");

		validator.validate(namesDto, errors);

		verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameTooLong", 255));
	}

	//================
	//--- UseApiId ---
	//================

	@Test
	public void validateStudyUseApiIdNull() {
		Errors errors = mock(Errors.class);
		when(errors.hasFieldErrors("useApiId")).thenReturn(true);

		NamesDTO namesDto = getValidNamesDTO();
		namesDto.setUseApiId(null);

		validator.validate(namesDto, errors);

		verify(errors).rejectValue("useApiId", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
	}

	//===========
	//-- ApiId --
	//===========

	@Test
	public void validateApiIdNull() {
		Errors errors = mock(Errors.class);
		NamesDTO namesDto = getValidNamesDTO();
		namesDto.setApiId(null);

		validator.validate(namesDto, errors);

		verify(errors).rejectValue("apiId", "errormessage", getMsg("validator.general.apiIdEmpty"));
	}

	@Test
	public void validateApiIdEmpty() {
		Errors errors = mock(Errors.class);
		NamesDTO namesDto = getValidNamesDTO();
		namesDto.setApiId(" ");

		validator.validate(namesDto, errors);

		verify(errors).rejectValue("apiId", "errormessage", getMsg("validator.general.apiIdEmpty"));
	}

	@Test
	public void validateApiIdTooLong() {
		Errors errors = mock(Errors.class);
		NamesDTO namesDto = getValidNamesDTO();
		namesDto.setApiId("loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong");

		validator.validate(namesDto, errors);

		verify(errors).rejectValue("apiId", "errormessage", getMsg("validator.general.apiIdTooLong", 255));
	}

	//=======================
	//-- Duplicate API IDs --
	//=======================

	@Test
	public void duplicateNameApiId() {
		Errors errors = mock(Errors.class);

		NamesDTO namesDtoA = getValidNamesDTO();
		namesDtoA.setUseApiId(false);

		NamesDTO namesDtoB = getValidNamesDTO();
		namesDtoB.setApiId(namesDtoA.getGuiName());

		validator.validateNames(List.of(namesDtoA, namesDtoB), errors, "duplicateNameApiId");

		verify(errors).rejectValue("duplicateNameApiId[0].apiId", "errormessage", getMsg("validator.general.apiIdNotUnique"));
		verify(errors).rejectValue("duplicateNameApiId[1].apiId", "errormessage", getMsg("validator.general.apiIdNotUnique"));
	}


	private NamesDTO getValidNamesDTO() {
		final var namesDto = new NamesDTO();
		namesDto.setGuiName("GuiName");
		namesDto.setUseApiId(true);
		namesDto.setApiId("ApiId");
		return namesDto;
	}
}
