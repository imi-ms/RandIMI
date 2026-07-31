package de.unimuenster.imi.randimi.model.study.stratum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Subclass of stratum part base. It represents a list of enumerations.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Entity
@ToString
public class StratumPartEnumeration extends StratumPartBase {

	/**
	 * The list of enumerations.
	 */
	@Column
	@Getter
	@Setter
	private String enumValue;

	/**
	 * ID used for identifying the stratum part via AIP communication.
	 */
	@Getter @Setter
	private String apiId;

	/**
	 * If the API ID should be synchronized with the name.
	 */
	@Getter @Setter
	private boolean synchronizeApiId;

	/**
	 * Checks, if an element is contained in the stratum part. If the data type
	 * is not compatible, false is returned.
	 *
	 * @param value Element to check
	 * @return If the element is contained or not.
	 */
	@Override
	public boolean isValueContainedInStratumPart(Object value) {
		String stringValue;
		try {
			stringValue = (String) value;
		} catch (Exception e) {
			return false;
		}
		return getPartKey().equals(stringValue);
	}

	@Override
	public String getPartKey() {
		return getApiId();
	}

	@Override
	public String getName() {
		return getEnumValue();
	}
}
