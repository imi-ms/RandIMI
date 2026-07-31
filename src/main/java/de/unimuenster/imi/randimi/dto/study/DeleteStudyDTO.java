package de.unimuenster.imi.randimi.dto.study;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class DeleteStudyDTO {

	@Valid
	@Getter @Setter
	private ChangeReason changeReason;

	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	@Getter @Setter
	private long studyId;
}
