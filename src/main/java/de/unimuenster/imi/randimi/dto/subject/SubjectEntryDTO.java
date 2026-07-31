package de.unimuenster.imi.randimi.dto.subject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;
import de.unimuenster.imi.randimi.dto.Views;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class SubjectEntryDTO {

	@JsonIgnore
	@Getter
	@Setter
	private long id;

	@JsonIgnore
	@Getter
	@Setter
	private long randomizationListId;

	@Schema(description = "Numeric number of the subject.", example = "1")
	@JsonProperty(index = 0)
	@Getter
	@Setter
	private int orderNumber;

	@Schema(description = "Assigned study arm of the subject.", example = "Control Group")
	@JsonProperty(index = 2)
	@Getter
	@Setter
	private String studyArmName;

	@Schema(description = "API ID of the assigned study arm of the subject.", example = "Control Group")
	@JsonProperty(index = 3)
	@JsonView(Views.ApiId.class)
	@Getter @Setter
	private String studyArmApiId;

	@Schema(description = "Timestamp of the randomization.", example = "2000-12-24 18:00:00")
	@JsonProperty(index = 5)
	@Getter
	@Setter
	private Timestamp randomizationTimestamp;

	@Schema(description = "Timestamp of the deletion.", example = "2000-12-24 18:00:00")
	@JsonProperty(index = 6)
	@Getter @Setter
	@Nullable
	private Timestamp deletionTimestamp;

	@Schema(description = "Timestamp of the release.", example = "2000-12-24 18:00:00")
	@JsonProperty(index = 7)
	@Getter @Setter
	@Nullable
	private Timestamp releaseTimestamp;

	@Schema(description = "Pseudonym of the subject.", example = "UKM_048")
	@JsonProperty(index = 1)
	@Getter
	@Setter
	private String pseudonym;

	@Schema(description = "Site of the subject.", example = "Münster")
	@JsonProperty(index = 8)
	@Getter
	@Setter
	private String location;

	@Schema(description = "Site API ID of the subject.", example = "23")
	@JsonProperty(index = 9)
	@JsonView(Views.ApiId.class)
	@Getter @Setter
	private String locationApiId;

	@JsonIgnore
	@Getter
	@Setter
	private Long siteId;

	@Schema(description = "Status of the subject.", example = "ACTIVE")
	@JsonProperty(index = 4)
	@Getter
	@Setter
	private SubjectStatus status;

	/**
	 * Strata of the subject for subject list export.
	 */
	@JsonIgnore
	private final Map<String, String> strata = new HashMap<>();

	@JsonAnyGetter
	public Map<String, String> getStrata() {
		return strata;
	}

}
