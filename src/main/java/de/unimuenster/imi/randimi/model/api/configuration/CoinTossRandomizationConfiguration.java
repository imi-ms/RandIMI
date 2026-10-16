package de.unimuenster.imi.randimi.model.api.configuration;

import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Configuration of the coin toss randomization.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration of the coin toss randomization. 'algorithm' MUST be 'COINTOSS'.")
public final class CoinTossRandomizationConfiguration extends RandomizationConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RandomizationAlgorithm getAlgorithm() {
		return RandomizationAlgorithm.COINTOSS;
	}
}
