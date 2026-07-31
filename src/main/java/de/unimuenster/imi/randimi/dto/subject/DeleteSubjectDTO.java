package de.unimuenster.imi.randimi.dto.subject;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class DeleteSubjectDTO {

	@Valid
	private ChangeReason changeReason;

	private boolean release;

	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	private long studyId;

	private String studyApiId;

	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	private long siteId;

	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	private long subjectListId;

	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	private long subjectId;
}
