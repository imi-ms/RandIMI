package de.unimuenster.imi.randimi.dto.study;

import de.unimuenster.imi.randimi.dto.study.statistics.SiteStatisticsDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for study statistics.
 *
 * @author Daniel Preciado-Marquez
 */
@Data
public class StudyStatisticsDTO {
	private List<SubjectListStatisticsDTO> subjectListStatistics = new ArrayList<>();

	private List<SiteStatisticsDTO> siteStatistics = new ArrayList<>();
}
