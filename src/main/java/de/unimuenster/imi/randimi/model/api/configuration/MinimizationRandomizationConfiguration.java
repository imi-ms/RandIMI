package de.unimuenster.imi.randimi.model.api.configuration;

import de.unimuenster.imi.randimi.model.enumeration.ImbalanceFunction;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration of the minimization.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration of the minimization. 'algorithm' MUST be 'MINIMIZATION'.")
@Getter @Setter
public final class MinimizationRandomizationConfiguration extends RandomizationConfiguration {

	/**
	 * If the ratio of study arms should be forced.
	 */
	@Schema(description = "If the ratio of the study arms should be forced.")
	private boolean forceRatio = true;

	/**
	 * The probability to draw the study arm with the highest imbalance.
	 */
	@Schema(description = "Probability to draw the study arm which minimizes the imbalance.", example = "0.7")
	@NotNull
	@DecimalMin("0")
	@DecimalMax("1")
	private Float imbalanceBias = 0.7f;

	/**
	 * Function for calculating the imbalance.
	 */
	@Schema(description = "Function for calculating the imbalance.")
	@NotNull
	private ImbalanceFunction imbalanceFunction = ImbalanceFunction.VARIANCE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RandomizationAlgorithm getAlgorithm() {
		return RandomizationAlgorithm.MINIMIZATION;
	}
}
