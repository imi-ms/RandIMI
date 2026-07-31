package de.unimuenster.imi.randimi.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body of the APT call "randomizePatient".
 * 
 * @author <a href="mailto:tobiashardt@uni-muenster.de">Tobias Hardt</a>
 * @author <a href="mailto:tobias.brix@uni-muenster.de">Tobias Brix</a>
 */
@Deprecated(since = "2.0.0")
@Data
@Schema(description = "Request model that contains parameters used for randomization of a subject.")
public class RandomizePatientRequestBodyV1 {
	/**
	 * API ID of the study to which the patient should be assigned.
	 */
	@NotNull
	@Schema(description = "API ID of the study",
	        example = "6")
	@JsonProperty(value = "studyId")
	private String studyApiId;

	/**
	 * Location API ID of the patient.
	 * In the future, it might be possible to change the name of a site, so the name of the location can not be found.
	 */
	@Schema(description = "Identifier of the institution that executes the randomization",
	        example = "Münster")
	@JsonProperty(value = "location")
	private String locationApiId;

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
	        example = "\"{\"Sex\":\"M\",\"Age\":34}\"")
	private String studyStrataParams;
}
