package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.service.algorithms.Randomization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Service for subjects.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class SubjectService {

	private final StudyRepository studyRepository;

	private final AuditService auditService;
	private final RandimiExceptionFactoryService exceptionFactoryService;
	private final RandomizationService randomizationService;

	public SubjectService(StudyRepository studyRepository, AuditService auditService,
	                      RandimiExceptionFactoryService exceptionFactoryService,
	                      RandomizationService randomizationService) {
		this.studyRepository = studyRepository;
		this.auditService = auditService;
		this.exceptionFactoryService = exceptionFactoryService;
		this.randomizationService = randomizationService;
	}

	/**
	 * Deletes and optionally releases the given subject.
	 *
	 * @param subject      The subject to be deleted.
	 * @param release      If the subject should be released and
	 * @param changeReason Reason why the subject is deleted.
	 * @throws RandimiException If the study is not active.
	 */
	@Transactional
	public void deleteSubject(final Subject subject, final boolean release,
	                          final String changeReason) throws RandimiException {
		final Study study = subject.getSubjectList().getStudy();

		if (!(study.isActive() || study.isInTestMode())) {
			throw exceptionFactoryService.notAcceptableStudyNotActive(study);
		}

		if (!doesStatusChange(subject, release)) {
			return;
		}

		final var oldSubject = auditService.createSubjectAuditEntry(subject);

		var now = Timestamp.from(java.time.Instant.now());

		if (subject.getDeletionTimestamp() == null) {
			subject.setDeletionTimestamp(now);
		}

		// Check whether the entry should be released or not
		if (release) {
			if (study.getPreGenerateSubjectList()) {
				// Pre generate new entry
				SubjectList subjectList = subject.getSubjectList();
				randomizationService.preGenerateSubject(subjectList, subject.getStudyArm());
			} else {
				Randomization randomization = randomizationService.getAlgorithmImplementation(
						study.getRandomizationAlgorithm());
				randomization.onSubjectRelease(subject);
			}

			subject.setReleaseTimestamp(now);
			subject.setStatus(SubjectStatus.RELEASED);
		} else {
			subject.setStatus(SubjectStatus.DELETED);
		}

		// Create AuditEntry
		final var newSubject = auditService.createSubjectAuditEntry(subject);
		auditService.createAuditEntryDeleteOrReleaseSubject(release, changeReason, study.getId(), subject.getId(),
		                                                    oldSubject, newSubject);

		studyRepository.save(study);
	}

	/**
	 * Updates the pseudonym of the given subject.
	 *
	 * @param subject      The subject whose pseudonym should be updated.
	 * @param newPseudonym The new pseudonym.
	 * @param changeReason Reason why the pseudonym is being updated.
	 * @throws RandimiException If the study is not active, the pseudonym is invalid, or the pseudonym is already registered.
	 */
	@Transactional
	public void updatePseudonym(final Subject subject, final String newPseudonym,
	                            final String changeReason) throws RandimiException {
		if (Objects.equals(subject.getPseudonym(), newPseudonym)) {
			return;
		}

		final Study study = subject.getSubjectList().getStudy();
		if (!(study.isActive() || study.isInTestMode())) {
			throw exceptionFactoryService.notAcceptableStudyNotActive(study);
		}

		randomizationService.validatePseudonym(newPseudonym, study, subject.getSite());

		final var oldSubject = auditService.createSubjectAuditEntry(subject);

		subject.setPseudonym(newPseudonym);

		final var newSubject = auditService.createSubjectAuditEntry(subject);
		auditService.createAuditEntryUpdatePseudonym(changeReason, study.getId(), subject.getId(), oldSubject,
		                                             newSubject);

		studyRepository.save(study);
	}

	private boolean doesStatusChange(final Subject subject, final boolean release) {
		return release ? subject.getStatus() != SubjectStatus.RELEASED
		               : subject.getStatus() == SubjectStatus.ACTIVE;
	}

}
