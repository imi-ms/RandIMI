package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.service.RandomizationService;
import org.springframework.validation.Errors;

/**
 * Interface for classes implementing randomization algorithms that create
 * {@link SubjectList RandomizationLists} based on study parameters.
 *
 * Implementations are used by the
 * {@link RandomizationService}.
 *
 * @author Daniel Preciado-Marquez
 */
public interface Randomization {

	/**
	 * Return an enum value that describes the algorithm the class implements.
	 *
	 * @return enum value
	 */
	RandomizationAlgorithm getAlgorithm();

	/**
	 * Called after a SubjectList was created.
	 *
	 * @param subjectList The created SubjectList.
	 */
	void onSubjectListCreation(SubjectList subjectList);

	/**
	 * Called when a StudyDTO gets validated.
	 *
	 * @param errors Errors object containing the errors of the passed StudyDTO.
	 * @param studyDTO The StudyDTO to validate.
	 */
	void onStudyDTOValidation(Errors errors, StudyDTO studyDTO);

	/**
	 * Called when a subject gets released.
	 *
	 * @param subject Subject to delete.
	 */
	void onSubjectRelease(Subject subject);

	/**
	 * Returns a random study arm of the corresponding study.
	 *
	 * @param site        Site containing the seed to use.
	 * @param subjectList The list the subject will be assigned to.
	 * @return A random study arm.
	 */
	StudyArm getRandomStudyArm(Site site, SubjectList subjectList);
}
