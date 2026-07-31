package de.unimuenster.imi.randimi.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Additional information of an error.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Additional information of an error.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor @Getter @Setter
public class ErrorResponseDetails {

	/**
	 * Map containing validation errors.
	 * The key is the JSON path of the field, the value is a list of validation errors.
	 * Only available if the request validation failed.
	 */
	@Schema(description = "JSON object containing the validation errors of the request. The key is the JSON path of the field, the value is a list of validation errors. Only available if the request validation failed.",
	        example = "{\"studyId\": [\"The requested study with id '0' does not exist.\"]}",
	        nullable = true)
	@Nullable
	private Map<String, List<String>> validationErrors = null;

	/**
	 * Conflicting subject of a randomization request.
	 * Null if the user has no permission to read the subject.
	 * Only available for errors of randomization requests with code 4010.
	 */
	@Schema(description = "Conflicting subject of a randomization. Null if the user has no permission to read the subject. Only available for errors of randomization requests with code 4010.",
	        nullable = true)
	@Nullable
	private SubjectResource existingSubject = null;

}
