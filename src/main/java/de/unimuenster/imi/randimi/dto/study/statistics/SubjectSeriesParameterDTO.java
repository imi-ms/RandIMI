package de.unimuenster.imi.randimi.dto.study.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * DTO containing the parameters for creating a subject series.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "The parameter for creating a subject series.")
@Data
public class SubjectSeriesParameterDTO {

	@Schema(description = "The API ID of the site for which the series should be created. If null subjects of all sites are included.",
	        example = "muenster")
	@Nullable
	private String siteApiId = null;

	/**
	 * Map containing a stratum part for each stratum in the study,
	 * identifying the subject list for which the series should be created.
	 * If the study is stratified by sites, {@link #siteApiId} has to be set as well.
	 */
	@Schema(description = "Must contain one entry for each stratum of the study. In GET requests can be set by adding query parameters in the form of 'strataParameters.Sex=M'.",
	        example = "{\"Sex\":\"M\",\"Age\":34}")
	@Nullable
	private Map<String, String> strataParameters = null;
}
