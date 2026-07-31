package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;

import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/**
 * Implementation of a simple coin toss algorithm.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Service
public class CoinTossRandomization extends AbstractRandomization {

	private final RandomService randomService;

	@Autowired
	public CoinTossRandomization(final MessageService messageService, final RandomService randomService) {
		super(messageService);
		this.randomService = randomService;
	}

	@Override
	public RandomizationAlgorithm getAlgorithm() {
		return RandomizationAlgorithm.COINTOSS;
	}

	@Override
	public StudyArm getRandomStudyArm(Site site, SubjectList subjectList) {
		Study study = site.getStudy();
		int studyArmIndex = randomService.nextRandomInt(site, subjectList, study.getStudyArms().size());
		return study.getStudyArms().get(studyArmIndex);
	}

	@Override
	public void onSubjectListCreation(SubjectList subjectList) {
		// Nothing to do
	}

	@Override
	public void onStudyDTOValidation(Errors errors, StudyDTO studyDTO) {
		// Nothing to do
	}

	@Override
	public void onSubjectRelease(Subject subject) {
		// Nothing to do
	}

}
