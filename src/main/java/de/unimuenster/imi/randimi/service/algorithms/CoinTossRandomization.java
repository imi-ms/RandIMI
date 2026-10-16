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

import java.util.List;

/**
 * Implementation of a simple coin toss algorithm.
 * <p>
 * Version 2: The study arm is drawn weighted by the ratios of the study arms.
 * See {@link CoinTossRandomizationV1} for the previous version.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Service
public class CoinTossRandomization extends AbstractRandomization {

	protected final RandomService randomService;

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
	public int getVersion() {
		return 2;
	}

	@Override
	public StudyArm getRandomStudyArm(Site site, SubjectList subjectList) {
		Study study = site.getStudy();
		return study.getStudyArms().get(drawStudyArmIndex(site, subjectList));
	}

	/**
	 * Draws the index of the study arm for the next subject.
	 *
	 * @param site        The site of the subject.
	 * @param subjectList The list the subject will be assigned to.
	 * @return Index of the drawn study arm.
	 */
	protected int drawStudyArmIndex(Site site, SubjectList subjectList) {
		List<StudyArm> studyArms = site.getStudy().getStudyArms();
		int sumRatios = 0;
		for (final StudyArm studyArm : studyArms)
			sumRatios += studyArm.getRatio();

		int ratioIndex = randomService.nextRandomInt(site, subjectList, sumRatios);
		int studyArmIndex = 0;
		while (ratioIndex >= studyArms.get(studyArmIndex).getRatio()) {
			ratioIndex -= studyArms.get(studyArmIndex).getRatio();
			studyArmIndex++;
		}

		return studyArmIndex;
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
