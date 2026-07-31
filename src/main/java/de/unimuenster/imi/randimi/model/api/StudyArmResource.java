package de.unimuenster.imi.randimi.model.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resource used by the API to represent a study arm.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Defines a study arm.")
@Getter
@AllArgsConstructor
public class StudyArmResource {

	/**
	 * The name.
	 */
	@Schema(description = "Name of the study arm.", example = "Intervention")
	private final String name;

	/**
	 * The API ID.
	 */
	@Schema(description = "Api ID of the study arm.", example = "Intervention")
	private final String apiId;
}
