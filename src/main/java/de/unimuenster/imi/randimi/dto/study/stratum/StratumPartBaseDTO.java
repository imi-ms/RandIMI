package de.unimuenster.imi.randimi.dto.study.stratum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.dto.study.NamesDTO;
import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.model.SelectInputOption;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Getter @Setter
public class StratumPartBaseDTO extends NamesDTO implements SelectInputOption {

	@JsonIgnore
	private long Id;

	@JsonIgnore
	private long stratumId;

	private int orderNumber;

	/**
	 * Specific for interval strata.
	 */
	@JsonIgnore
	private Float intervalBegin;

	/**
	 * Specific for interval strata.
	 */
	@JsonIgnore
	private Float intervalEnd;

	/**
	 * Corresponding site of the stratum part.
	 * Specific for StratumPartSite.
	 */
	@JsonIgnore
	private SiteDTO site;

	/**
	 * Returns the display name based on the present values.
	 * For enumerated strata the enum value, for  and for site strata the site gui name
	 *
	 * @return the display name.
	 */
	@JsonIgnore
	public String getDisplayName() {
		if (guiName != null) {
			return guiName;
		}
		if (site != null) {
			return site.getGuiName();
		}
		return intervalBegin + " - " + intervalEnd;
	}

	@JsonIgnore
	public String getEnumValue() {
		return getGuiName();
	}

	@JsonIgnore
	public void setEnumValue(final String enumValue) {
		setGuiName(enumValue);
	}

	@JsonIgnore
	public boolean isEmptyEnum() {
		return isFormEmpty();
	}

	@JsonIgnore
	public boolean isEmptyInterval() {
		return isFormEmpty() && intervalBegin == null && intervalEnd == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean lookupTranslation() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@JsonIgnore
	@Override
	public String getOptionName() {
		return getDisplayName();
	}

	/**
	 * {@inheritDoc}
	 */
	@JsonIgnore
	@Override
	public String getOptionValue() {
		return getApiId();
	}
}
