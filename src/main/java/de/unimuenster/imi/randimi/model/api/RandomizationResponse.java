package de.unimuenster.imi.randimi.model.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Response containing the result of a randomization.")
@AllArgsConstructor
@Getter
public class RandomizationResponse {

	@Schema(description = "The randomized subject.")
	private final SubjectResource subject;
}
