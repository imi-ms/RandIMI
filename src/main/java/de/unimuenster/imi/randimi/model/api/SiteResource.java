package de.unimuenster.imi.randimi.model.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resource for representing a site in the API.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "API resource for representing a site.")
@AllArgsConstructor @Getter
public class SiteResource {

	@Schema(description = "Name of the site.", example = "Münster")
	private String name;

	@Schema(description = "API ID of the site.", example = "23")
	private String apiId;
}
