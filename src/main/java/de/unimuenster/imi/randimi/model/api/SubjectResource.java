package de.unimuenster.imi.randimi.model.api;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.Map;

/**
 * API Resource for subjects.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "API resource for representing a subject.")
@AllArgsConstructor @Getter
public class SubjectResource {

	/**
	 * Numeric number of the subject.
	 */
	@Schema(description = "Numeric number of the subject.", example = "1")
	private final int orderNumber;

	/**
	 * The assigned study arm of the subject.
	 */
	@Schema(description = "The assigned study arm of the subject.", example = "Control Group")
	private final StudyArmResource studyArm;

	/**
	 * Timestamp of the randomization.
	 */
	@Schema(description = "Timestamp of the randomization.", example = "2000-12-24 18:00:00")
	private final Timestamp randomizationTimestamp;

	/**
	 * Pseudonym of the subject.
	 */
	@Schema(description = "Pseudonym of the subject.", example = "UKM_048")
	private final String pseudonym;

	/**
	 * Site of the subject.
	 */
	@Schema(description = "Site of the subject.")
	private final SiteResource site;

	/**
	 * Additional stratification parameters of the study.
	 * The site stratum is not included.
	 * The keys of the map are the API IDs of the strata and the values are the .
	 */
	@Schema(description = "Additional stratification parameters of the study. The site stratum is not included.",
	        example = "{\"Sex\":\"M\",\"Age\":34}")
	private final Map<String, String> stratificationParameters;

	/**
	 * Status of the subject.
	 */
	@Schema(description = "Status of the subject.", example = "ACTIVE")
	private final SubjectStatus status;
}
