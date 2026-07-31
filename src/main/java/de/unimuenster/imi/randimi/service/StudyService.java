package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.DeleteStudyDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.model.enumeration.AuditClass;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;
import de.unimuenster.imi.randimi.service.algorithms.Randomization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing studies.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class StudyService {

	private final AclClassRepository aclClassRepository;
	private final AclEntryRepository aclEntryRepository;
	private final AclObjectIdentityRepository aclObjectIdentityRepository;
	private final StudyRepository studyRepository;

	private final StudyMapper studyMapper;

	private final AuditService auditService;
	private final RandimiExceptionFactoryService exceptionService;
	private final RandomizationService randomizationService;
	private final StratumCodeService stratumCodeService;

	@Autowired
	public StudyService(final AclClassRepository aclClassRepository,
	                    final AclEntryRepository aclEntryRepository,
	                    final AclObjectIdentityRepository aclObjectIdentityRepository,
	                    final StudyRepository studyRepository,
	                    final StudyMapper studyMapper,
	                    final AuditService auditService,
	                    final RandimiExceptionFactoryService exceptionService,
	                    final RandomizationService randomizationService,
	                    final StratumCodeService stratumCodeService) {
		this.aclClassRepository = aclClassRepository;
		this.aclEntryRepository = aclEntryRepository;
		this.aclObjectIdentityRepository = aclObjectIdentityRepository;
		this.studyRepository = studyRepository;
		this.studyMapper = studyMapper;
		this.auditService = auditService;
		this.exceptionService = exceptionService;
		this.randomizationService = randomizationService;
		this.stratumCodeService = stratumCodeService;
	}

	/**
	 * Puts the given study into the test mode.
	 * If the study is already in test mode, resets the subject lists.
	 *
	 * @param study The study to be put into test mode.
	 * @throws RandimiException If the study has already been activated or if the subject lists could not be generated.
	 */
	@Transactional
	public void changeToTestMode(final Study study) throws RandimiException {
		final StudyStatus oldStatus = study.getStatus();

		if (oldStatus != StudyStatus.CREATED && oldStatus != StudyStatus.TESTING) {
			throw exceptionService.notAcceptableStudyAlreadyActivated(study);
		}

		final var statusChangeOld = auditService.createStatusChangeAuditEntry(study);

		// Clear subject lists from potential test mode
		study.getSubjectLists().clear();

		// Set status before generating the subject lists, because pre-generated studies use the status
		study.setStatus(StudyStatus.TESTING);
		addMissingSubjectLists(study);
		studyRepository.save(study);

		final var statusChangeNew = auditService.createStatusChangeAuditEntry(study);

		auditService.createAuditEntryStudyStatusChange(study.getId(), AuditType.TEST, statusChangeOld, statusChangeNew);
	}

	/**
	 * Activates the given study.
	 * @param study The study to be activated.
	 * @throws RandimiException If the study has already been activated or if the subject lists could not be generated.
	 */
	@Transactional
	public void activateStudy(final Study study) throws Exception {
		final StudyStatus oldStatus = study.getStatus();

		if (oldStatus != StudyStatus.CREATED && oldStatus != StudyStatus.TESTING) {
			throw exceptionService.notAcceptableStudyAlreadyActivated(study);
		}

		final var statusChangeOld = auditService.createStatusChangeAuditEntry(study);

		// Clear subject lists from potential test mode
		study.getSubjectLists().clear();

		// Set status before generating the subject lists, because pre-generated studies use the status
		study.setStatus(StudyStatus.ACTIVE);
		addMissingSubjectLists(study);
		study.setActivationDate(new Timestamp(System.currentTimeMillis()));
		studyRepository.save(study);

		final var statusChangeNew = auditService.createStatusChangeAuditEntry(study);

		auditService.createAuditEntryStudyStatusChange(study.getId(), AuditType.ACTIVATE, statusChangeOld,
		                                               statusChangeNew);
	}

	/**
	 * Locks the given study
	 * @param study The study to be locked.
	 * @throws RandimiException If the study is already locked or not active.
	 */
	@Transactional
	public void lockStudy(final Study study) throws RandimiException {
		if (study.getStatus() == StudyStatus.LOCKED) {
			throw exceptionService.notAcceptableStudyAlreadyLocked(study);
		}
		if (study.getStatus() != StudyStatus.ACTIVE) {
			throw exceptionService.notAcceptableStudyNotActive(study);
		}

		final var statusChangeOld = auditService.createStatusChangeAuditEntry(study);

		study.setStatus(StudyStatus.LOCKED);
		studyRepository.save(study);

		final var statusChangeNew = auditService.createStatusChangeAuditEntry(study);

		auditService.createAuditEntryStudyStatusChange(study.getId(), AuditType.LOCK, statusChangeOld, statusChangeNew);
	}

	/**
	 * Unlocks a locked study.
	 * @param study The study to be unlocked.
	 * @throws RandimiException If the study is not locked.
	 */
	@Transactional
	public void unlockStudy(final Study study) throws RandimiException {
		if (study.getStatus() != StudyStatus.LOCKED) {
			throw exceptionService.notAcceptableStudyNotLocked(study);
		}

		final var statusChangeOld = auditService.createStatusChangeAuditEntry(study);

		study.setStatus(StudyStatus.ACTIVE);
		studyRepository.save(study);

		final var statusChangeNew = auditService.createStatusChangeAuditEntry(study);

		auditService.createAuditEntryStudyStatusChange(study.getId(), AuditType.UNLOCK, statusChangeOld,
		                                               statusChangeNew);
	}

	/**
	 * Archives the given study.
	 * @param study The study to be archived.
	 * @param retentionPeriod Date until the study should be archived.
	 * @throws RandimiException If the study is already archived.
	 */
	@Transactional
	public void archiveStudy(final Study study, @Nullable final LocalDate retentionPeriod) throws RandimiException {
		if (study.getStatus() != StudyStatus.ACTIVE && study.getStatus() != StudyStatus.LOCKED &&
		    study.getStatus() != StudyStatus.ARCHIVED) {
			throw exceptionService.notAcceptableStudyNotActive(study);
		}

		final var statusChangeOld = auditService.createStatusChangeAuditEntry(study);

		study.setRetentionPeriod(retentionPeriod != null ? Timestamp.valueOf(retentionPeriod.atStartOfDay()) : null);
		study.setStatus(StudyStatus.ARCHIVED);
		studyRepository.save(study);

		var statusChangeNew = auditService.createStatusChangeAuditEntry(study);

		auditService.createAuditEntryStudyStatusChange(study.getId(), AuditType.ARCHIVE, statusChangeOld, statusChangeNew);
	}

	/**
	 * Reactivates an archived study.
	 * @param study The study to be reactivated.
	 * @throws RandimiException If the study is not archived.
	 */
	@Transactional
	public void reactivateStudy(final Study study) throws RandimiException {
		if (study.getStatus() != StudyStatus.ARCHIVED) {
			throw exceptionService.notAcceptableStudyNotArchived(study);
		}

		final var statusChangeOld = auditService.createStatusChangeAuditEntry(study);

		study.setStatus(StudyStatus.ACTIVE);
		study.setRetentionPeriod(null);
		studyRepository.save(study);

		final var statusChangeNew = auditService.createStatusChangeAuditEntry(study);

		auditService.createAuditEntryStudyStatusChange(study.getId(), AuditType.REACTIVATE, statusChangeOld,
		                                               statusChangeNew);
	}

	@Transactional
	public void deleteStudy(final DeleteStudyDTO deleteStudyDTO) throws RandimiException {
		final long studyId = deleteStudyDTO.getStudyId();
		final Optional<Study> studyOptional = studyRepository.findById(studyId);

		if (studyOptional.isEmpty()) {
			throw exceptionService.unknownStudy();
		}

		final Study study = studyOptional.get();

		if (study.getStatus() == StudyStatus.ARCHIVED) {
			softDeleteStudy(study, deleteStudyDTO.getChangeReason());
		} else {
			hardDeleteStudy(study);
		}
	}

	private void hardDeleteStudy(final Study study) throws RandimiException {
		// Check if study is in status where deletion is allowed
		if (study.getStatus() != StudyStatus.INEXISTENT && study.getStatus() != StudyStatus.CREATED &&
		    study.getStatus() != StudyStatus.TESTING && study.getStatus() != StudyStatus.ARCHIVED) {
			throw exceptionService.notAcceptableStudyActive(study);
		}

		// Delete ACL stuff
		final AclClass aclClass = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(),
		                                                                           Study.class.getName());
		final AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				aclClass, study.getId());
		if (aclObjectIdentity != null) {
			aclEntryRepository.deleteByAclObjectIdentity(aclObjectIdentity);
			aclObjectIdentityRepository.delete(aclObjectIdentity);
		}

		final AclClass siteAclClass = aclClassRepository.findFirstByClassNameOrSynonym(Site.class.getName(),
		                                                                               Site.class.getName());
		for (final var site : study.getSites()) {
			final AclObjectIdentity siteAclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
					siteAclClass, site.getId());
			if (siteAclObjectIdentity != null) {
				aclEntryRepository.deleteByAclObjectIdentity(siteAclObjectIdentity);
				aclObjectIdentityRepository.delete(siteAclObjectIdentity);
			}
		}

		// Delete study
		study.getSubjectLists().clear();
		auditService.deleteAuditForStudy(study.getId());
		studyRepository.delete(study);
	}

	private void softDeleteStudy(final Study study, final ChangeReason changeReason) throws RandimiException {
		// Check if study is in status where deletion is allowed
		if (study.getStatus() != StudyStatus.ARCHIVED) {
			throw exceptionService.notAcceptableStudyNotArchived(study);
		}

		final StudyDTO studyDTO = studyMapper.toStudyDTO(study);

		// Delete ACL stuff
		final AclClass studyAclClass = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(),
		                                                                                Study.class.getName());
		final AclObjectIdentity studyAclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				studyAclClass, study.getId());
		aclEntryRepository.deleteByAclObjectIdentity(studyAclObjectIdentity);

		final AclClass siteAclClass = aclClassRepository.findFirstByClassNameOrSynonym(Site.class.getName(),
		                                                                               Site.class.getName());
		for (final var site : study.getSites()) {
			final AclObjectIdentity siteAclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
					siteAclClass, site.getId());
			if (siteAclObjectIdentity != null) {
				aclEntryRepository.deleteByAclObjectIdentity(siteAclObjectIdentity);
			}
		}

		// Remove users
		study.getAssignedUsers().clear();

		// Delete study
		auditService.deleteAuditForStudyAndAuditClass(study.getId(), AuditClass.SUBJECT);
		study.setStatus(StudyStatus.DELETED);
		study.getSubjectLists().clear();
		study.setRetentionPeriod(null);

		// Create AuditEntry
		auditService.createAuditEntryDeleteStudy(studyDTO, study.getId(), changeReason);
	}

	/**
	 * Adds all missing subject lists.
	 * @param study Study to which the subject lists should be added.
	 * @throws RandimiException If the algorithm of the study is not valid.
	 */
	public void addMissingSubjectLists(final Study study) throws RandimiException {
		final List<List<StratumPartBase>> stratumPartCombinations = stratumCodeService.calculateStratumPartCombinations(study);

		// Check if all combinations have a corresponding subject list
		for (final List<StratumPartBase> combination : stratumPartCombinations) {
			// Look for an existing subject list
			boolean missing = stratumCodeService.getSubjectListForParts(combination, study).isEmpty();

			// Add a new subject list if missing
			if (missing) {
				final SubjectList newSubjectList = new SubjectList();
				newSubjectList.addAllStratumParts(combination);
				study.addSubjectList(newSubjectList);

				final Randomization randomization = randomizationService.getAlgorithmImplementation(study.getRandomizationAlgorithm());

				randomization.onSubjectListCreation(newSubjectList);

				if (study.getPreGenerateSubjectList()) {
					randomizationService.preGenerateSubjectList(study, newSubjectList, randomization);
				}
			}
		}
	}

	public List<String> getTakenApiIds() {
		final List<String> apiIds = new ArrayList<>();
		apiIds.addAll(studyRepository.getApiIds());
		apiIds.addAll(studyRepository.getNames());
		return apiIds;
	}

	public String generateApiId() {
		return getTakenApiIds().stream()
		                       .filter(s -> s.matches("\\d+"))
		                       .map(Long::parseLong)
		                       .max(Comparator.naturalOrder())
		                       .map(aLong -> Long.toString(aLong + 1L))
		                       .orElse("1");
	}
}
