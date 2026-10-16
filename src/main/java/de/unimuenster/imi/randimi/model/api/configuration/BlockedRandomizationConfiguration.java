package de.unimuenster.imi.randimi.model.api.configuration;

import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration of the blocked randomization.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration of the blocked randomization. 'algorithm' MUST be 'BLOCKED'.")
@Getter @Setter
public final class BlockedRandomizationConfiguration extends RandomizationConfiguration {

	/**
	 * Minimal block size.
	 */
	@Schema(description = "Minimal block size.", example = "2")
	@NotNull
	@Min(2)
	private Integer minBlocksize;

	/**
	 * Maximal block size.
	 */
	@Schema(description = "Maximal block size.", example = "6")
	@NotNull
	@Min(2)
	private Integer maxBlocksize;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RandomizationAlgorithm getAlgorithm() {
		return RandomizationAlgorithm.BLOCKED;
	}

	/**
	 * Checks that the minimal block size is not larger than the maximal block size.
	 *
	 * @return If the block sizes are valid.
	 */
	@Schema(hidden = true)
	@AssertTrue(message = "minBlocksize must not be larger than maxBlocksize")
	private boolean isBlocksizeRangeValid() {
		return minBlocksize == null || maxBlocksize == null || minBlocksize <= maxBlocksize;
	}
}
