package de.unimuenster.imi.randimi.model.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * Request body of the APT call "/study/{studyId}/subject".
 * 
 * @author <a href="mailto:tobiashardt@uni-muenster.de">Tobias Hardt</a>
 * @author <a href="mailto:tobias.brix@uni-muenster.de">Tobias Brix</a>
 */
@Data
@Schema(description = "Request model that contains parameters used for randomization of a subject.")
public class RandomizePatientRequestBodyV2 {
	/**
	 * Id of the Location of the patient.
	 */
	@Schema(description = "API ID of the institution that executes the randomization",
	        example = "Münster(42)")
	private String siteApiId;

	/**
	 * Pseudonym of the patient. 
	 */
	@Schema(description = "Pseudonym of the subject",
	        example = "MXMSTRMNN123")
	private String pseudonym;
	
	/**
	 * Additional strata parameters of the study.
	 */
	@Schema(description = "JSON representation of additional study-specific stratum parameters, eg. sex, age...",
	        example = "{\"Sex\":\"M\",\"Age\":34}")
	@Nullable private Map<String, String> studyStrataParams;
}
