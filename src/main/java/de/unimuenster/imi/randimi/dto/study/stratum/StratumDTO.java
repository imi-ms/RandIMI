package de.unimuenster.imi.randimi.dto.study.stratum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.dto.study.NamesDTO;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Getter @Setter
public class StratumDTO extends NamesDTO {

	@JsonIgnore
	private long Id;

	private StratumType stratumType;

	@JsonIgnore
	private long studyId;

	private int orderNumber;

	private List<StratumPartBaseDTO> stratumParts;

	@JsonIgnore
	@Override
	public boolean isFormEmpty() {
		return super.isFormEmpty() && stratumParts.isEmpty();
	}
}
