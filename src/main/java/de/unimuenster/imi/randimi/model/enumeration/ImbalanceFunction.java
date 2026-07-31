package de.unimuenster.imi.randimi.model.enumeration;

import de.unimuenster.imi.randimi.model.SelectInputOption;

/**
 * Enum determining the function for calculating the imbalance in the minimization algorithm.
 *
 * @author Daniel Preciado-Marquez
 */
public enum ImbalanceFunction implements SelectInputOption {
	RANGE,
	VARIANCE,
	;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean lookupTranslation() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOptionName() {
		return "enum.ImbalanceFunction." + name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOptionValue() {
		return name();
	}
}
