package de.unimuenster.imi.randimi.model.api.configuration;

import de.unimuenster.imi.randimi.model.enumeration.PseudonymHandling;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration used by the API for creating a study.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration for creating a study.")
@Getter @Setter
public class StudyConfiguration {

	/**
	 * Name of the study.
	 */
	@Schema(description = "Name of the study.", example = "Randomization Study")
	@Size(max = 255)
	@NotBlank
	private String name;

	/**
	 * API ID of the study.
	 * If null, the name is used as API ID.
	 */
	@Schema(description = "API ID of the study. If not set, the name is used as API ID.", example = "study-123")
	@Size(max = 255)
	@Nullable
	private String apiId;

	/**
	 * Description of the study.
	 */
	@Schema(description = "Description of the study.")
	@Size(max = 524288)
	@Nullable
	private String description;

	/**
	 * If the subject lists should be generated in advance.
	 */
	@Schema(description = "If the subject lists should be generated in advance. Not supported by all randomization algorithms.")
	private boolean preGenerateSubjectList = false;

	/**
	 * Pseudonym handling of the study.
	 */
	@Schema(description = "Pseudonym handling of the study.")
	@NotNull
	private PseudonymHandling pseudonymHandling = PseudonymHandling.UNIQUE_IN_STUDY;

	/**
	 * Maximum number of subjects in the study.
	 */
	@Schema(description = "Maximum number of subjects in the study.", example = "100")
	@NotNull
	@Min(1)
	private Integer capacity;

	/**
	 * If the study is stratified by site.
	 */
	@Schema(description = "If the study is stratified by site.")
	private boolean stratifyBySite = false;

	/**
	 * Sites of the study.
	 */
	@Schema(description = "Sites of the study. At least one site is required.")
	@NotEmpty
	private List<@Valid @NotNull SiteConfiguration> sites = new ArrayList<>();

	/**
	 * Study arms of the study.
	 */
	@Schema(description = "Study arms of the study. At least two study arms are required.")
	@Size(min = 2)
	@NotNull
	private List<@Valid @NotNull StudyArmConfiguration> arms = new ArrayList<>();

	/**
	 * Strata of the study.
	 * The site stratum is not included and is configured with {@link #stratifyBySite}.
	 */
	@Schema(description = "Strata of the study. The site stratum is not included and is configured with 'stratifyBySite'.")
	@NotNull
	private List<@Valid @NotNull StratumConfiguration> strata = new ArrayList<>();

	/**
	 * Configuration of the randomization algorithm.
	 */
	@Schema(description = "Configuration of the randomization algorithm.")
	@Valid
	@NotNull
	private RandomizationConfiguration randomization;
}
