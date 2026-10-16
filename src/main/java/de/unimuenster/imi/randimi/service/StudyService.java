package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.controller.helper.RandimiHelper;
import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.DeleteStudyDTO;
import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.model.enumeration.AuditClass;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.PermissionBundle;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.algorithms.Randomization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for managing studies.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class StudyService {

	private static final Logger LOGGER = LogManager.getLogger(StudyService.class);

	/**
	 * Name of the site stratum.
	 */
	public static final String STRATUM_NAME_LOCATION = "location";

	private final AclClassRepository aclClassRepository;
	private final AclEntryRepository aclEntryRepository;
	private final AclObjectIdentityRepository aclObjectIdentityRepository;
	private final StudyRepository studyRepository;
	private final RandimiUserRepository userRepository;

	private final StudyMapper studyMapper;

	private final AuditService auditService;
	private final RandimiExceptionFactoryService exceptionService;
	private final RandomizationService randomizationService;
	private final StratumCodeService stratumCodeService;
	private final StudyUtilityService studyUtilityService;

	@Autowired
	public StudyService(final AclClassRepository aclClassRepository,
	                    final AclEntryRepository aclEntryRepository,
	                    final AclObjectIdentityRepository aclObjectIdentityRepository,
	                    final StudyRepository studyRepository,
	                    final RandimiUserRepository userRepository,
	                    final StudyMapper studyMapper,
	                    final AuditService auditService,
	                    final RandimiExceptionFactoryService exceptionService,
	                    final RandomizationService randomizationService,
	                    final StratumCodeService stratumCodeService,
	                    final StudyUtilityService studyUtilityService) {
		this.aclClassRepository = aclClassRepository;
		this.aclEntryRepository = aclEntryRepository;
		this.aclObjectIdentityRepository = aclObjectIdentityRepository;
		this.studyRepository = studyRepository;
		this.userRepository = userRepository;
		this.studyMapper = studyMapper;
		this.auditService = auditService;
		this.exceptionService = exceptionService;
		this.randomizationService = randomizationService;
		this.stratumCodeService = stratumCodeService;
		this.studyUtilityService = studyUtilityService;
	}

	/**
	 * Prepares the given study DTO for validation and persistence.
	 * Removes empty entries, sets the order numbers and default values and simplifies the ratios of the study arms.
	 *
	 * @param studyDTO           The study DTO to prepare.
	 * @param removeEmptyEntries If empty study arms, strata and stratum parts should be removed.
	 *                           Should only be true if these can still be modified.
	 */
	public void prepareStudyDTO(final StudyDTO studyDTO, final boolean removeEmptyEntries) {
		// Remove empty study arms, strata and stratum parts
		if (removeEmptyEntries) {
			studyDTO.getStudyArms().removeIf(StudyArmDTO::isFormEmpty);

			for (StratumDTO stratumDTO : studyDTO.getEnumeratedStratums()) {
				stratumDTO.getStratumParts().removeIf(StratumPartBaseDTO::isEmptyEnum);
			}
			studyDTO.getEnumeratedStratums().removeIf(StratumDTO::isFormEmpty);

			for (StratumDTO stratumDTO : studyDTO.getIntervalStratums()) {
				stratumDTO.getStratumParts().removeIf(StratumPartBaseDTO::isEmptyInterval);
			}
			studyDTO.getIntervalStratums().removeIf(StratumDTO::isFormEmpty);
		}

		// Remove deleted sites
		studyDTO.getSites().removeIf(SiteDTO::isFormEmpty);

		// Set default values and order numbers
		int orderNumber = 0;
		for (SiteDTO site : studyDTO.getSites()) {
			site.setOrderNumber(orderNumber);
			orderNumber += 1;

			if (site.getSeed() == null) {
				site.setSeed(RandimiHelper.generateSeed());
			}
			if (site.getPseudonymRegex() == null || site.getPseudonymRegex().isBlank()) {
				site.setPseudonymRegex(".*");
			}
		}

		orderNumber = 0;
		for (StudyArmDTO studyArm : studyDTO.getStudyArms()) {
			studyArm.setOrderNumber(orderNumber);
			orderNumber += 1;

			if (studyArm.getRatio() == null) {
				studyArm.setRatio(1);
			}
		}

		orderNumber = 0;
		for (final StratumDTO stratumDTO : studyDTO.getEnumeratedStratums()) {
			stratumDTO.setOrderNumber(orderNumber);
			orderNumber += 1;

			int partOrderNumber = 0;
			for (final StratumPartBaseDTO stratumPartBaseDTO : stratumDTO.getStratumParts()) {
				stratumPartBaseDTO.setOrderNumber(partOrderNumber);
				partOrderNumber += 1;
			}
		}

		// Simplify ratios
		studyUtilityService.simplifyStudyArmRatios(studyDTO);
	}

	/**
	 * Creates a new study from the given study DTO.
	 * The DTO must be prepared with {@link #prepareStudyDTO(StudyDTO, boolean)} and validated beforehand.
	 * Persists the study, creates the ACL identities, grants the creating user all study permissions and
	 * creates the audit entry.
	 *
	 * @param studyDTO    The prepared and validated study DTO.
	 * @param currentUser The user creating the study.
	 * @return The persisted study.
	 */
	@Transactional
	public Study createStudy(final StudyDTO studyDTO, final RandimiUser currentUser) {
		addSiteStratum(studyDTO);

		final Study study = studyMapper.toStudy(studyDTO, new Study());
		studyRepository.save(study);

		// Set missing fields in studyDTO for audit entry
		studyDTO.setStatus(study.getStatus());
		studyDTO.setActivationDate(study.getActivationDate());

		final RandimiUser user = userRepository.findById(currentUser.getId()).get();

		// Persist the acl object identity of the study
		final AclObjectIdentity aclObjectIdentity = new AclObjectIdentity(study.getId(), Boolean.TRUE,
		                                                                  aclClassRepository.findFirstByClassNameOrSynonym(
				                                                                  Study.class.getName(),
				                                                                  StudyDTO.class.getName()),
		                                                                  user.getAclSid(), null);
		aclObjectIdentityRepository.save(aclObjectIdentity);

		// Set initial permissions
		if (!user.hasUserRole(UserRoles.ROLE_ADMIN)) {
			study.addAssignedUser(user);

			// Iterate over all bundles for study permissions and cross-site permissions
			for (PermissionBundle permissionBundle : PermissionBundle.values()) {
				for (PermissionType permissionType : PermissionBundle.getPermissionTypes(permissionBundle))
					applyStudyPermission(permissionType, user.getAclSid(), aclObjectIdentity);
			}
		}

		updateSiteAclIdentities(study, new ArrayList<>(), user);

		auditService.createAuditEntryCreateStudy(studyDTO, study.getId());

		return study;
	}

	/**
	 * Adds the site stratum to the given study DTO if the study is stratified by site.
	 * The site stratum is not transferred from the frontend and must be added after every edit.
	 *
	 * @param studyDTO The study DTO.
	 */
	public void addSiteStratum(final StudyDTO studyDTO) {
		if (!studyDTO.isStratifyBySite()) {
			return;
		}

		final StratumDTO locationStratum = new StratumDTO();
		locationStratum.setGuiName(STRATUM_NAME_LOCATION);
		locationStratum.setApiId(STRATUM_NAME_LOCATION);
		locationStratum.setUseApiId(false);
		locationStratum.setOrderNumber(studyDTO.getEnumeratedStratums().size());
		locationStratum.setStratumType(StratumType.SITE);
		locationStratum.setStratumParts(new ArrayList<>());

		for (final SiteDTO site : studyDTO.getSites()) {
			final StratumPartBaseDTO sitePart = new StratumPartBaseDTO();
			sitePart.setOrderNumber(locationStratum.getStratumParts().size());
			sitePart.setSite(site);
			locationStratum.getStratumParts().add(sitePart);
		}

		studyDTO.setSiteStratum(locationStratum);
	}

	/**
	 * Creates the ACL identities of new sites and removes the ACL identities and entries of deleted sites.
	 *
	 * @param study         The study containing the current sites.
	 * @param originalSites The sites of the study before the modification.
	 * @param currentUser   The user modifying the study.
	 */
	public void updateSiteAclIdentities(final Study study, final List<Site> originalSites,
	                                    final RandimiUser currentUser) {
		final AclClass siteAclClass = aclClassRepository.findFirstByClassNameOrSynonym(Site.class.getName(),
		                                                                               SiteDTO.class.getName());

		final List<Site> currentSites = study.getSites();

		for (final Site site : currentSites) {
			final boolean isNewSite = originalSites.stream().noneMatch(a -> Objects.equals(a.getId(), site.getId()));
			if (isNewSite) {
				AclObjectIdentity siteAclObjectIdentity = new AclObjectIdentity(site.getId(), Boolean.TRUE,
				                                                                siteAclClass, currentUser.getAclSid(),
				                                                                null);
				aclObjectIdentityRepository.save(siteAclObjectIdentity);
			}
		}

		for (final Site site : originalSites) {
			final boolean isDeletedSite = currentSites.stream().noneMatch(a -> Objects.equals(a.getId(), site.getId()));
			if (isDeletedSite) {
				final AclObjectIdentity siteAclObjectIdentity = aclObjectIdentityRepository
						.findFirstByObjectIdClassAndObjectIdIdentity(siteAclClass, site.getId());
				aclEntryRepository.deleteByAclObjectIdentity(siteAclObjectIdentity);
				aclObjectIdentityRepository.delete(siteAclObjectIdentity);
			}
		}
	}

	/**
	 * Grants the given study permission to the given SID.
	 * Permissions which are not related to studies are ignored.
	 *
	 * @param permissionType          The permission to grant.
	 * @param aclSid                  The SID the permission should be granted to.
	 * @param studyAclObjectIdentity  The ACL object identity of the study.
	 */
	public void applyStudyPermission(final PermissionType permissionType, final AclSid aclSid,
	                                 final AclObjectIdentity studyAclObjectIdentity) {
		switch (permissionType) {
			// Permissions for study
			case CREATE_SUBJECT:
			case DELETE_STUDY:
			case DELETE_SUBJECT:
			case GET_NOTIFICATION:
			case READ_AUDIT_COMPLEX:
			case READ_AUDIT_SIMPLE:
			case READ_REPORT:
			case READ_STUDY:
			case READ_SUBJECT:
			case UPDATE_STUDY:
			case UPDATE_STUDY_USERS:
			case UPDATE_SUBJECT:
				aclEntryRepository
						.save(new AclEntry(aclSid, studyAclObjectIdentity, 1, permissionType, true, false, false));
				break;
			default:
				LOGGER.warn("Unhandled permission type {}!", permissionType.getValue());
				break;
		}
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
	public void activateStudy(final Study study) throws RandimiException {
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

				final Randomization randomization = randomizationService.getAlgorithmImplementation(study);

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
