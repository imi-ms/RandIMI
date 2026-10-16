package de.unimuenster.imi.randimi.dto.study;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.enumeration.ImbalanceFunction;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * DTO for minimization parameter.
 *
 * @author Daniel Preciado-Marquez
 */
@Getter @Setter
public class MinimizationParameterDTO {

	/**
	 * ID of the corresponding database entity.
	 */
	@JsonIgnore
	@Nullable
	private Long id;

	/**
	 * If the ratio of study arms should be forced.
	 */
	private Boolean forceRatio = true;

	/**
	 * The probability to draw the study arm with the highest imbalance.
	 */
	private Float imbalanceBias = 0.7f;

	/**
	 * Function for calculating the imbalance.
	 */
	private ImbalanceFunction imbalanceFunction = ImbalanceFunction.VARIANCE;
}
