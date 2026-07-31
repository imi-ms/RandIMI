package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyUtilityService {

	public List<Integer> getRatios(StudyDTO studyDTO) {
		return studyDTO.getStudyArms().stream().map(StudyArmDTO::getRatio).collect(Collectors.toList());
	}

	public int calculateNumberOfStudyArmParts(StudyDTO studyDTO) {
		List<Integer> ratios = getRatios(studyDTO);
		return calculateNumberOfStudyArmParts(ratios);
	}

	public int calculateNumberOfStudyArmParts(Study study) {
		List<Integer> ratios = study.getStudyArms().stream().map(StudyArm::getRatio).collect(Collectors.toList());
		return calculateNumberOfStudyArmParts(ratios);
	}

	public void simplifyStudyArmRatios(StudyDTO study) {
		List<Integer> ratios = getRatios(study);
		int gdc = greatestCommonDenominator(ratios);

		if (gdc == -1) {
			return;
		}

		for (StudyArmDTO studyArm : study.getStudyArms()) {
			studyArm.setRatio(studyArm.getRatio() / gdc);
		}
	}
	private boolean isValid(List<Integer> ratios) {
		return ratios.stream().allMatch(number -> number != null && number > 0);
	}

	private int calculateNumberOfStudyArmParts(List<Integer> ratios) {
		if (!isValid(ratios)) {
			return -1;
		}
		return ratios.stream().reduce(Integer::sum).orElse(-1);
	}

	private int greatestCommonDenominator(int a, int b) {
		return b == 0 ? a : greatestCommonDenominator(b, a % b);
	}

	private int greatestCommonDenominator(List<Integer> numbers) {
		if (!isValid(numbers)) {
			return -1;
		}

		return numbers.stream().reduce(this::greatestCommonDenominator).orElse(-1);
	}
}
