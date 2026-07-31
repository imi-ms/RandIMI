package de.unimuenster.imi.randimi.model.api;

import de.unimuenster.imi.randimi.model.enumeration.ExportFileType;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Schema(description = "Request model that contains parameters used for the subject export.")
@Getter @Setter
public class SubjectExportRequest {
	@Schema(description = "If the subject lists should be split into separate files. The response will return a zip file.",
			defaultValue = "false", allowableValues = {"false", "true"})
	@NotNull
	private Boolean splitFiles = false;

	@Schema(description = "If the subject lists should include columns for the API IDs.",
	        defaultValue = "false", allowableValues = {"false", "true"})
	@NotNull
	private Boolean includeApiIds = false;

	@Schema(description = "The format of the exported lists.", defaultValue = "JSON", allowableValues = {"CSV", "JSON"})
	@NotNull
	private ExportFileType format = ExportFileType.JSON;

	@Schema(description = "The delimiter used for CSV files.", defaultValue = ",")
	@NotNull
	private Character delimiter = ',';

	@ArraySchema(schema = @Schema(description = "List of Status of the subjects that should be exported.",
	                              implementation = SubjectStatus.class), minItems = 1)
	@NotNull
	private Set<@NotNull SubjectStatus> status = null;

	@ArraySchema(schema = @Schema(description = "List of API IDs of the sites their subjects should be exported."),
	                              minItems = 1)
	@NotNull
	private Set<@NotBlank String> sites = null;

	@NotNull
	@Schema(description = "Strata parameter in JSON format.",
	        example = "{\"age\": [\"18-30\",\"31-50\"],\"gender\": [\"f\"]}")
	private String strata = null;
}
