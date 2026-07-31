package de.unimuenster.imi.randimi.mapping.study.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unimuenster.imi.randimi.dto.study.user.StudyUsersDTO;
import de.unimuenster.imi.randimi.model.study.Study;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class StudyUsersMapper {

	private final StudyUserMapper studyUserMapper;

	@Autowired
	public StudyUsersMapper(final StudyUserMapper studyUserMapper) {
		this.studyUserMapper = studyUserMapper;
	}

	public StudyUsersDTO toStudyUsersDTO(final Study study) {
		StudyUsersDTO studyUsersDTO = new StudyUsersDTO(study);
		studyUsersDTO.setStudyUserDTOs(studyUserMapper.toStudyUserDTOs(study));
		return studyUsersDTO;
	}

}
