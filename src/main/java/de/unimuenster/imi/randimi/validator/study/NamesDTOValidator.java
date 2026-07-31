package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.NamesDTO;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NamesDTOValidator extends AbstractValidator {

	public NamesDTOValidator(MessageService messageService) {
		super(messageService);
	}

	public void validate(final NamesDTO namesDTO, final Errors errors) {
		// Name
		final String studyArmName = namesDTO.getGuiName();
		if (studyArmName == null || studyArmName.trim().isEmpty()) {
			errors.rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
		} else if (studyArmName.length() > 255) {
			errors.rejectValue("guiName", "errormessage", getMsg("validator.general.nameTooLong", 255));
		}

		// Use API ID
		if (namesDTO.getUseApiId() == null) {
			errors.rejectValue("useApiId", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		// API ID
		if (!errors.hasFieldErrors("useApiId") && namesDTO.getUseApiId()) {
			if (namesDTO.getApiId() == null || namesDTO.getApiId().trim().isEmpty()) {
				errors.rejectValue("apiId", "errormessage", getMsg("validator.general.apiIdEmpty"));
			} else if (namesDTO.getApiId().length() > 255) {
				errors.rejectValue("apiId", "errormessage", getMsg("validator.general.apiIdTooLong", 255));
			}
		}
	}

	public void validateNames(final List<? extends NamesDTO> namesDTOList, final Errors errors,
	                          final String errorPath) {
		final Map<String, List<Integer>> names = new HashMap<>();
		final Map<String, List<Integer>> apiIds = new HashMap<>();

		for (int i = 0; i < namesDTOList.size(); i++) {
			// Collect names
			final NamesDTO dto = namesDTOList.get(i);

			if (!errors.hasFieldErrors(errorPath + "[" + i + "].guiName")) {
				if (names.containsKey(dto.getGuiName())) {
					names.get(dto.getGuiName()).add(i);
				} else {
					List<Integer> studyArms = new ArrayList<>(1);
					studyArms.add(i);
					names.put(dto.getGuiName(), studyArms);
				}
			}
			if (!errors.hasFieldErrors(errorPath + "[" + i + "].useApiId")) {
				String apiId = null;

				if (dto.getUseApiId()) {
					if (!errors.hasFieldErrors(errorPath + "[" + i + "].apiId")) {
						apiId = dto.getApiId();
					}
				} else {
					if (!errors.hasFieldErrors(errorPath + "[" + i + "].guiName")) {
						apiId = dto.getGuiName();
					}
				}

				if (apiId != null) {
					if (apiIds.containsKey(apiId)) {
						apiIds.get(apiId).add(i);
					} else {
						List<Integer> studyArms = new ArrayList<>(1);
						studyArms.add(i);
						apiIds.put(apiId, studyArms);
					}
				}
			}
		}

		// Check for duplicate study arm names
		for (var entry : names.values()) {
			if (entry.size() > 1) {
				for (Integer i : entry) {
					errors.rejectValue(errorPath + "[" + i + "].guiName", "errormessage",
					                   getMsg("validator.general.nameNotUnique"));
				}
			}
		}

		// Check for duplicate study arm API IDs
		for (var entry : apiIds.values()) {
			if (entry.size() > 1) {
				for (Integer i : entry) {
					errors.rejectValue(errorPath + "[" + i + "].apiId", "errormessage",
					                   getMsg("validator.general.apiIdNotUnique"));
				}
			}
		}
	}

	@Override public boolean supports(Class<?> clazz) {
		return StudyArmDTO.class.isAssignableFrom(clazz);
	}

	@Override public void validate(final Object target, final Errors errors) {
		validate((NamesDTO) target, errors);
	}
}
