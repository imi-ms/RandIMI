package de.unimuenster.imi.randimi.model.study.stratum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Subclass of stratum part base.
 * It represents an interval of type float.
 * 
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Entity
@ToString
public class StratumPartInterval extends StratumPartBase {
	/**
	 * The begin of the stratum interval.
	 */
	@Column
	@Getter @Setter
	private float intervalBegin;

	/**
	 * The end of the stratum interval.
	 */
	@Column
	@Getter @Setter
	private float intervalEnd;

	/**+
	 * Checks, if an element is contained in the stratum part. 
	 * If the data type is not compatible, false is returned.
	 * 
	 * @param value Element to check
	 * @return If the element is contained or not.
	 */	
	@Override
	public boolean isValueContainedInStratumPart(Object value) {
		if (value instanceof Number numberValue) {
			final float floatValue = numberValue.floatValue();
			return floatValue >= intervalBegin && floatValue <= intervalEnd;
		}
		if (value instanceof String stringValue) {
			return getPartKey().equals(stringValue);
		}

		return false;
	}

	@Override
	public String getPartKey() {
		return getIntervalBegin() + "-" + getIntervalEnd();
	}

	@Override
	public String getName() {
		return getPartKey();
	}
}
