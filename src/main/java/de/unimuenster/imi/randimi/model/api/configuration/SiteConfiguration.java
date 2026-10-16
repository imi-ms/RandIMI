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
 * Configuration used by the API for creating a site.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration for creating a site.")
@Getter @Setter
public class SiteConfiguration {

	/**
	 * Name of the site.
	 */
	@Schema(description = "Name of the site.", example = "Münster")
	@Size(max = 255)
	@NotBlank
	private String name;

	/**
	 * API ID of the site.
	 * If null, the name is used as API ID.
	 */
	@Schema(description = "API ID of the site. If not set, the name is used as API ID.", example = "23")
	@Size(max = 255)
	@Nullable
	private String apiId;

	/**
	 * Maximum number of subjects in the site.
	 */
	@Schema(description = "Maximum number of subjects in the site.", example = "50")
	@NotNull
	@Min(2)
	private Integer capacity;

	/**
	 * Regular expression the pseudonyms of the site must match.
	 */
	@Schema(description = "Regular expression the pseudonyms of the site must match.", example = "^MS\\d{4}$")
	@NotBlank
	private String pseudonymRegex;

	/**
	 * Seed for the random number generator of the site.
	 * If null, a random seed is generated.
	 */
	@Schema(description = "Seed for the random number generator of the site. If not set, a random seed is generated.")
	@Nullable
	private Long seed;
}
