package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.model.enumeration.PseudonymHandling;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the duplicate pseudonym check of the {@link StudyDTOValidator} when the pseudonym handling is set to
 * {@link PseudonymHandling#UNIQUE_IN_STUDY}. Only subjects with a status in {@link SubjectStatus#BLOCKING_STATUS}
 * must be considered.
 * <p>
 * In contrast to {@link StudyDtoValidatorTest}, the repositories are not mocked, so the actual query is tested.
 *
 * @author Daniel Preciado-Marquez
 */
public class StudyDtoValidatorPseudonymHandlingTest extends ValidatorTestBase {

	private static final String DUPLICATE_PSEUDONYM = "duplicate-pseudonym";

	@Autowired EntityManager entityManager;
	@Autowired SubjectRepository subjectRepository;
	@Autowired StudyMapper studyMapper;
	@Autowired StudyDTOValidator validator;

	@Test
	public void validateUniqueInStudyWithoutDuplicates() {
		final Study study = getActiveStudy();

		assertFalse(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	@Test
	public void validateUniqueInStudyWithDuplicateActiveSubjects() {
		final Study study = getActiveStudy();
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.ACTIVE);
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.ACTIVE);

		assertTrue(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	@Test
	public void validateUniqueInStudyWithDuplicateDeletedSubjects() {
		final Study study = getActiveStudy();
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.DELETED);
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.DELETED);

		assertTrue(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	@Test
	public void validateUniqueInStudyWithDuplicateActiveAndDeletedSubject() {
		final Study study = getActiveStudy();
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.ACTIVE);
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.DELETED);

		assertTrue(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	@Test
	public void validateUniqueInStudyWithDuplicateActiveAndReleasedSubject() {
		final Study study = getActiveStudy();
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.ACTIVE);
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.RELEASED);

		assertFalse(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	@Test
	public void validateUniqueInStudyWithDuplicateReleasedSubjects() {
		final Study study = getActiveStudy();
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.RELEASED);
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.RELEASED);

		assertFalse(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	@Test
	public void validateUniqueInStudyWithPreGeneratedSubjects() {
		final Study study = getActiveStudy();
		addSubject(study, null, SubjectStatus.PRE_GENERATED);
		addSubject(study, null, SubjectStatus.PRE_GENERATED);

		assertFalse(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	@Test
	public void validateUniqueInStudyWithDuplicateActiveSubjectsInInactiveStudy() {
		final Study study = getPregeneratedStudy();
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.ACTIVE);
		addSubject(study, DUPLICATE_PSEUDONYM, SubjectStatus.ACTIVE);

		assertFalse(hasDuplicatePseudonymError(validateUniqueInStudy(study)));
	}

	private Errors validateUniqueInStudy(final Study study) {
		final StudyDTO dto = studyMapper.toStudyDTO(study);
		dto.setPseudonymHandling(PseudonymHandling.UNIQUE_IN_STUDY);

		final Errors errors = new BeanPropertyBindingResult(dto, "dto");
		validator.validate(dto, errors);
		return errors;
	}

	private boolean hasDuplicatePseudonymError(final Errors errors) {
		final String message = getMsg("validator.study.pseudonymHandling.duplicatePseudonyms");
		return errors.getFieldErrors("pseudonymHandling").stream()
		             .map(FieldError::getDefaultMessage)
		             .anyMatch(message::equals);
	}

	private void addSubject(final Study study, @Nullable final String pseudonym, final SubjectStatus status) {
		final SubjectList subjectList = study.getSubjectLists().get(0);

		final Subject subject = new Subject(subjectList.getSubjects().size() + 1, study.getStudyArms().get(0));
		subject.setSubjectList(subjectList);
		subject.setStatus(status);
		if (pseudonym != null) {
			subject.setAssignedTo(study.getSites().get(0), pseudonym);
		}

		subjectRepository.save(subject);
		entityManager.flush();
	}
}
