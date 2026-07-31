package de.unimuenster.imi.randimi.model;

/**
 * Interface used for select inputs to determine the value and the displayed name of the option.
 *
 * @author Daniel Preciado-Marquez
 */
public interface SelectInputOption {

	/**
	 * @return If the returned name of {@link #getOptionName} should be looked up as a message resource key.
	 */
	boolean lookupTranslation();

	/**
	 * Can be the label or a key in the message sources if {@link #lookupTranslation()} is true.
	 * @return The label of the option.
	 */
	String getOptionName();

	/**
	 * @return The value of the option.
	 */
	String getOptionValue();
}
