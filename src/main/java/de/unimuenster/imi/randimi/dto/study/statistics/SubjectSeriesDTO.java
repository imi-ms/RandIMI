package de.unimuenster.imi.randimi.dto.study.statistics;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.Map;

/**
 * Data Transfer Object (DTO) for a time series of subjects.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Time series about the number of subjects over time.")
@Data
public class SubjectSeriesDTO {

	@Schema(description = "The site to which the series belongs. May be null.")
	@Nullable
	private SiteDTO site;

	@Schema(description = "Map containing the stratum parts to which the series belongs.", example = "{\"Sex\":\"M\",\"Age\":34}")
	@Nullable
	private Map<String, String> strataParts;

	@Schema(description = "The start date of the series, i.e. the day of the first randomization",
	        example = "2024-03-19")
	private LocalDate start;

	@Schema(description = "The end date of the series, i.e. the day of the last randomization or deallocation.",
	        example = "2024-03-22")
	private LocalDate end;

	@Schema(description = "The series containing one entry for each day between the start and end dates.",
	        example = "[1, 3, 3, 5]")
	private int[] series;

	@Schema(description = "The target number of subjects. Depending on the requested series, it can be the capacity of the entire study, the requested site, or the specific list corresponding to the requested stratum parameter.",
	        example = "10")
	private int target;
}
