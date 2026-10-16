package de.unimuenster.imi.randimi.model.api.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration used by the API for creating an enumerated stratum.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration for creating a stratum with enumerated values.")
@Getter @Setter
public class StratumConfiguration {

	/**
	 * Name of the stratum.
	 */
	@Schema(description = "Name of the stratum.", example = "Sex")
	@Size(max = 255)
	@NotBlank
	private String name;

	/**
	 * API ID of the stratum.
	 * If null, the name is used as API ID.
	 */
	@Schema(description = "API ID of the stratum. If not set, the name is used as API ID.", example = "Sex")
	@Size(max = 255)
	@Nullable
	private String apiId;

	/**
	 * Possible values of the stratum.
	 */
	@Schema(description = "Possible values of the stratum. At least two values are required.")
	@Size(min = 2)
	@NotNull
	private List<@Valid @NotNull Value> values = new ArrayList<>();

	/**
	 * Configuration of a value of an enumerated stratum.
	 */
	@Schema(name = "StratumValueConfiguration", description = "Configuration for creating a value of a stratum.")
	@Getter @Setter
	public static class Value {

		/**
		 * Name of the value.
		 */
		@Schema(description = "Name of the value.", example = "Female")
		@Size(max = 255)
		@NotBlank
		private String name;

		/**
		 * API ID of the value.
		 * If null, the name is used as API ID.
		 */
		@Schema(description = "API ID of the value. If not set, the name is used as API ID.", example = "F")
		@Size(max = 255)
		@Nullable
		private String apiId;
	}
}
