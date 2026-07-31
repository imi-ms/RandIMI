package de.unimuenster.imi.randimi.mapping.study;

import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.model.api.StudyArmResource;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import org.springframework.stereotype.Component;

@Component
public class StudyArmMapper {

    private final NamesMapper namesMapper;

	public StudyArmMapper(NamesMapper namesMapper) {
		this.namesMapper = namesMapper;
	}

	/**
     * Converts a {@link StudyArm} object to an {@link StudyArmDTO} object.
     *
     * @return An {@link StudyArmDTO} object based on this {@link StudyArm} object.
     */
    public StudyArmDTO toStudyArmDTO(StudyArm studyArm) {
        StudyArmDTO studyArmDTO = new StudyArmDTO();

        studyArmDTO.setId(studyArm.getId());
        studyArmDTO.setStudyId(studyArm.getStudy().getId());

        namesMapper.toNamesDTO(studyArm, studyArmDTO);

        studyArmDTO.setOrderNumber(studyArm.getOrderNumber());
        studyArmDTO.setRatio(studyArm.getRatio());

        return studyArmDTO;
    }

    public StudyArm toStudyArm(StudyArmDTO dto, int orderNumber) {
        StudyArm studyArm = new StudyArm();
        studyArm.setId(dto.getId());
        studyArm.setOrderNumber(orderNumber);

        namesMapper.toNamedEntity(dto, studyArm);

        studyArm.setRatio(dto.getRatio());
        return studyArm;
    }

    public void toStudyArm(final StudyArmDTO dto, final StudyArm studyArm) {
        namesMapper.toNamedEntity(dto, studyArm);
    }

    /**
     * Converts the study arm to a study arm resource.
     *
     * @param studyArm The study arm to convert.
     * @return The converted study arm resource.
     */
    public StudyArmResource toStudyArmResource(final StudyArm studyArm) {
        return new StudyArmResource(studyArm.getGuiName(), studyArm.getApiId());
    }
}
