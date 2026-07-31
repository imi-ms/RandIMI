package de.unimuenster.imi.randimi.dto.study.statistics;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import lombok.Data;

import java.util.Map;

/**
 * Statistics about the current study progress for the given site.
 *
 * @author Daniel Preciado-Marquez
 */
@Data
public class SiteStatisticsDTO {
	/**
	 * The site these statistics belong to.
	 */
	private SiteDTO site;

	/**
	 * Number of subjects in the study from the corresponding site.
	 */
	private long numberSubjects;

	/**
	 * Number of subjects for each study arm.
	 * Key is the API ID of the study arm.
	 */
	private Map<String, Long> numberSubjectsPerStudyArm;

	/**
	 * Returns the maximum number of subjects per study arm.
	 *
	 * @return the maximum number of subjects per study arm.
	 */
	public long getCapacityPerStudyArm() {
		return site.getCapacity() / numberSubjectsPerStudyArm.size();
	}
}
