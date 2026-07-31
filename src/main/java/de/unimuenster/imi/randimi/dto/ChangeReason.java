package de.unimuenster.imi.randimi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class ChangeReason {

	@NotNull(message = "{validator.general.mustNotBeNull}")
	@NotBlank(message = "{validator.general.mustNotBeEmpty}")
	@Size(max = 255, message = "{validator.general.mustNotBeLongerThan255Chars}")
	@Getter @Setter
	private String changeReason;

	@Getter @Setter
	private String oldDto;
}
