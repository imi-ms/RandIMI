package de.unimuenster.imi.randimi.model.api.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Base class of the configuration of a randomization algorithm.
 * The concrete algorithm is selected by the property {@code algorithm}.
 * <p>
 * To support a new algorithm, create a subclass and register it in {@link JsonSubTypes} and in the {@link Schema}.
 *
 * @author Daniel Preciado-Marquez
 */
@Schema(description = "Configuration of the randomization algorithm. The property 'algorithm' determines the algorithm and the additional properties.",
        discriminatorProperty = "algorithm",
        discriminatorMapping = {
		        @DiscriminatorMapping(value = "BLOCKED", schema = BlockedRandomizationConfiguration.class),
		        @DiscriminatorMapping(value = "COINTOSS", schema = CoinTossRandomizationConfiguration.class),
		        @DiscriminatorMapping(value = "MINIMIZATION", schema = MinimizationRandomizationConfiguration.class),
        },
        oneOf = {
		        BlockedRandomizationConfiguration.class,
		        CoinTossRandomizationConfiguration.class,
		        MinimizationRandomizationConfiguration.class,
        })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "algorithm")
@JsonSubTypes({
		@JsonSubTypes.Type(value = BlockedRandomizationConfiguration.class, name = "BLOCKED"),
		@JsonSubTypes.Type(value = CoinTossRandomizationConfiguration.class, name = "COINTOSS"),
		@JsonSubTypes.Type(value = MinimizationRandomizationConfiguration.class, name = "MINIMIZATION"),
})
public abstract sealed class RandomizationConfiguration
		permits BlockedRandomizationConfiguration, CoinTossRandomizationConfiguration,
		        MinimizationRandomizationConfiguration {

	/**
	 * Returns the randomization algorithm this configuration belongs to.
	 *
	 * @return The randomization algorithm.
	 */
	@JsonIgnore
	public abstract RandomizationAlgorithm getAlgorithm();
}
