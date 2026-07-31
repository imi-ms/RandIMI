package de.unimuenster.imi.randimi.dto.study;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter @Setter
public class StudyArmDTO extends NamesDTO {

	@JsonIgnore
	private long id = 0;

	@JsonIgnore
	private long studyId = 0;

	private int orderNumber = 0;

	private Integer ratio;

	/**
	 * {@inheritDoc}
	 */
	@JsonIgnore
	@Override
	public boolean isFormEmpty() {
		return super.isFormEmpty() && ratio == null;
	}
}
