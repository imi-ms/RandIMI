package de.unimuenster.imi.randimi.model.api.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * Configuration used by the API for creating a study arm.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration for creating a study arm.")
@Getter @Setter
public class StudyArmConfiguration {

	/**
	 * Name of the study arm.
	 */
	@Schema(description = "Name of the study arm.", example = "Intervention")
	@Size(max = 255)
	@NotBlank
	private String name;

	/**
	 * API ID of the study arm.
	 * If null, the name is used as API ID.
	 */
	@Schema(description = "API ID of the study arm. If not set, the name is used as API ID.", example = "Intervention")
	@Size(max = 255)
	@Nullable
	private String apiId;

	/**
	 * Allocation ratio of the study arm.
	 */
	@Schema(description = "Allocation ratio of the study arm.", example = "1")
	@NotNull
	@Min(1)
	private Integer ratio = 1;
}
