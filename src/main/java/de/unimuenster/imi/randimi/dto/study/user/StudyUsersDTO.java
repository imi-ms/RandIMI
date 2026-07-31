package de.unimuenster.imi.randimi.dto.study.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.Site;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@NoArgsConstructor
public class StudyUsersDTO {

	@JsonIgnore
	@Getter
	@Setter
	private boolean modified = false;

	@JsonIgnore
	@Getter
	@Setter
	private Map<Long, String> siteNames = new HashMap<>();

	@JsonIgnore
	@Min(value = 1, message = "{validator.general.mustNotBeZero}")
	@Getter
	@Setter
	private Long studyId;

	@Getter @Setter
	private String studyApiId;

	@Getter
	@Setter
	private List<StudyUserDTO> studyUserDTOs = new ArrayList<>();

	public StudyUsersDTO(final Study study) {
		this.studyId = study.getId();
		this.studyApiId = study.getApiId();

		for (final Site site : study.getSites())
			siteNames.put(site.getId(), site.getGuiName());
	}
}
