package de.unimuenster.imi.randimi.dto.study.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class RemoveStudyUserDTO {

	@NotNull(message = "{validator.general.mustNotBeNull}")
	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	@Getter @Setter
	private Long removedUserId = 0L;
}
