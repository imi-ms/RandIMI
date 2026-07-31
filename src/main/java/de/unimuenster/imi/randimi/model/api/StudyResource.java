package de.unimuenster.imi.randimi.model.api;

import de.unimuenster.imi.randimi.model.enumeration.PseudonymHandling;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents a study resource in the API.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "API resource for representing a study.")
@AllArgsConstructor @Getter
public class StudyResource {

	/**
	 * Name of the study.
	 */
	@Schema(description = "Name of the study.", example = "Randomization Study")
	private final String name;

	/**
	 * API ID of the study.
	 */
	@Schema(description = "API ID of the study.", example = "study-123")
	private final String apiId;

	/**
	 * Pseudonym handling of the study.
	 */
	@Schema(description = "Pseudonym handling of the study.")
	private final PseudonymHandling pseudonymHandling;

	/**
	 * List of sites associated with the study.
	 */
	@Schema(description = "List of sites associated with the study.")
	private final List<SiteResource> sites;

	/**
	 * List of study arms associated with the study.
	 */
	@Schema(description = "List of study arms associated with the study.")
	private final List<StudyArmResource> arms;

	/**
	 * List of strata associated with the study.
	 * The site stratum is not included.
	 */
	@Schema(description = "List of strata associated with the study. The site stratum is not included.")
	private final List<StrataInfoResponseV2.Definition> strata;
}
