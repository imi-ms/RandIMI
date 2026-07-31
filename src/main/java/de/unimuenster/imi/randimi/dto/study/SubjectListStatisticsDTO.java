package de.unimuenster.imi.randimi.dto.study;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the statistics of a subject list.
 *
 * @author Daniel Preciado-Marquez
 */
@Data
public class SubjectListStatisticsDTO {

	private List<StratumPartBaseDTO> stratumParts;

	private Map<String, Long> numberSubjectsPerStudyArm;

	private long numberSubjects;

	private int capacity;

	/**
	 * Returns a string with the names of the stratum parts.
	 *
	 * @return a string with the names of the stratum parts.
	 */
	public String getStratumPartNames() {
		return stratumParts.stream()
		                   .map(StratumPartBaseDTO::getDisplayName)
		                   .reduce((a, b) -> a + ", " + b)
		                   .orElse("");
	}

	/**
	 * Returns a string with data attributes for HTML elements.
	 *
	 * @return a string with data attributes for HTML elements.
	 */
	public String getDataAttribute() {
		final List<String> parts = new ArrayList<>();

		for (final StratumPartBaseDTO stratumPart : stratumParts) {
			if (stratumPart.getSite() != null ) {
				// Site stratum
				parts.add("data-randimi-stratum-part-" + stratumPart.getStratumId() + "='" + stratumPart.getSite().getGuiName() + "'");
			} else {
				parts.add("data-randimi-stratum-part-" + stratumPart.getStratumId() + "='" + stratumPart.getGuiName() + "'");
			}
		}

		return parts.stream().reduce((a, b) -> a + "," + b).orElse("data-randimi-stratum-part=null");
	}

	/**
	 * Returns the maximum number of subjects per study arm.
	 *
	 * @return the maximum number of subjects per study arm.
	 */
	public long getCapacityPerStudyArm() {
		return capacity / numberSubjectsPerStudyArm.size();
	}
}
