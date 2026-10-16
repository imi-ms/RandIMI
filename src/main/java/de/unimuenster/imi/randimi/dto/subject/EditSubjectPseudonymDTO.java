package de.unimuenster.imi.randimi.dto.subject;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class EditSubjectPseudonymDTO {

	@Valid
	private ChangeReason changeReason;

	@NotBlank(message = "{validator.general.mustNotBeEmpty}")
	@Size(max = 255, message = "{validator.general.mustNotBeLongerThan255Chars}")
	private String pseudonym;

	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	private long studyId;

	@NotBlank(message = "{validator.general.mustNotBeEmpty}")
	private String studyApiId;

	@Nullable
	private Long subjectListId;

	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	private long subjectId;
}
