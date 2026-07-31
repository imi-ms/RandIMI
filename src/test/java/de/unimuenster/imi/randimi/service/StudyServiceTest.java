package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.DeleteStudyDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@WithUserDetails(value = "admin", userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
public class StudyServiceTest extends RandimiIntegrationTest {

	@Autowired AclClassRepository aclClassRepository;
	@Autowired AclEntryRepository aclEntryRepository;
	@Autowired AclObjectIdentityRepository aclObjectIdentityRepository;
	@Autowired SiteRepository siteRepository;
	@Autowired StudyService studyService;

	/**
	 * =======================================================
	 * --- Tests for {@link StudyService#lockStudy(Study)} ---
	 * =======================================================
	 */

	@Test
	public void lockStudy() {
		final Study activeStudy = getActiveStudy();
		assertDoesNotThrow(() -> studyService.lockStudy(activeStudy));
		assertEquals(StudyStatus.LOCKED, activeStudy.getStatus(), "Study has not been locked!");
		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.LOCK, null);
	}

	@Test
	public void lockInactiveStudy() {
		final Study inactiveStudy = getInactiveStudy();
		var exception = assertThrows(RandimiException.class, () -> studyService.lockStudy(inactiveStudy));
		assertEquals(RandimiException.NOT_ACCEPTABLE_STUDY_NOT_ACTIVE, exception.getErrorCode(),
		             "Unexpected error code!");
		assertEquals(StudyStatus.CREATED, inactiveStudy.getStatus(), "Study should not have been locked!");
	}

	@Test
	public void lockLockedStudy() {
		final Study lockedStudy = getLockedActiveStudy();
		var exception = assertThrows(RandimiException.class, () -> studyService.lockStudy(lockedStudy));
		assertEquals(RandimiException.NOT_ACCEPTABLE_STUDY_ALREADY_LOCKED, exception.getErrorCode(),
		             "Unexpected error code!");
		assertEquals(StudyStatus.LOCKED, lockedStudy.getStatus(), "Study should still be locked!");
	}

	/**
	 * =========================================================
	 * --- Tests for {@link StudyService#unlockStudy(Study)} ---
	 * =========================================================
	 */

	@Test
	public void unlockStudy() {
		final Study lockedActiveStudy = getLockedActiveStudy();
		assertDoesNotThrow(() -> studyService.unlockStudy(lockedActiveStudy));
		assertEquals(StudyStatus.ACTIVE, lockedActiveStudy.getStatus(), "Study has not been unlocked!");
//		testLastAuditEntryForStudy(lockedActiveStudy.getId(), AuditType.UNLOCK, null);
	}

	@Test
	public void unlockUnlockedStudy() {
		final Study unlockedStudy = getActiveStudy();
		var exception = assertThrows(RandimiException.class, () -> studyService.unlockStudy(unlockedStudy));
		assertEquals(RandimiException.NOT_ACCEPTABLE_STUDY_NOT_LOCKED, exception.getErrorCode(),
		             "Unexpected error code!");
		assertEquals(StudyStatus.ACTIVE, unlockedStudy.getStatus(), "Study should still be unlocked!");
	}

	/**
	 * ===============================================================================
	 * --- Tests for {@link StudyService#archiveStudy(Study, java.time.LocalDate)} ---
	 * ===============================================================================
	 */

	@Test
	public void archiveStudy() {
		var expectedContent =
				"""
				{
				  "status" : "%s"
				}""".formatted(StudyStatus.ARCHIVED.name()).replaceAll("\n", System.lineSeparator());
		var expectedOldContent =
				"""
				{
				  "status" : "%s"
				}""".formatted(StudyStatus.ACTIVE.name()).replaceAll("\n", System.lineSeparator());

		final Study activeStudy = getActiveStudy();
		assertDoesNotThrow(() -> studyService.archiveStudy(activeStudy, null));
		assertEquals(StudyStatus.ARCHIVED, activeStudy.getStatus(), "Study has not been archived!");
		assertNull(activeStudy.getRetentionPeriod(), "Retention period should be null!");
		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.ARCHIVE, null, expectedContent, expectedOldContent);
	}

	@Test
	public void archiveArchivedStudy() {
		final Study archivedStudy = getArchivedStudy();
		var retentionPeriod = LocalDate.of(2024, 10, 29);

		assertDoesNotThrow(() -> studyService.archiveStudy(archivedStudy, retentionPeriod));
		assertEquals(StudyStatus.ARCHIVED, archivedStudy.getStatus(), "Study should not have been archived!");
		assertNotNull(archivedStudy.getRetentionPeriod(), "RetentionPeriod should not be null!");
		assertEquals(retentionPeriod.atStartOfDay(), archivedStudy.getRetentionPeriod().toLocalDateTime(),
		             "Study should have been archived!");
	}

	@Test
	public void archiveInactiveStudy() {
		final Study inactiveStudy = getInactiveStudy();
		var exception = assertThrows(RandimiException.class, () -> studyService.archiveStudy(inactiveStudy, null));
		assertEquals(RandimiException.NOT_ACCEPTABLE_STUDY_NOT_ACTIVE, exception.getErrorCode(),
		             "Unexpected error code!");
		assertEquals(StudyStatus.CREATED, inactiveStudy.getStatus(), "Study should not have been archived!");
	}

	/**
	 * =============================================================
	 * --- Tests for {@link StudyService#reactivateStudy(Study)} ---
	 * =============================================================
	 */

	@Test
	public void reactivateStudy() {
		var expectedContent =
				"""
				{
				  "status" : "%s"
				}""".formatted(StudyStatus.ACTIVE.name()).replaceAll("\n", System.lineSeparator());
		var expectedOldContent =
				"""
				{
				  "status" : "%s"
				}""".formatted(StudyStatus.ARCHIVED.name()).replaceAll("\n", System.lineSeparator());

		final Study activeStudy = getArchivedStudy();
		assertDoesNotThrow(() -> studyService.reactivateStudy(activeStudy));
		assertEquals(StudyStatus.ACTIVE, activeStudy.getStatus(), "Study has not been reactivated!");
		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.REACTIVATE, null, expectedContent,
		                           expectedOldContent);
	}

	/**
	 * ===========================================================
	 * --- Tests for {@link StudyService#deleteStudy(de.unimuenster.imi.randimi.dto.study.DeleteStudyDTO)} ---
	 * ===========================================================
	 */

	@Test
	public void deleteInactiveStudy() {
		final Study inactiveStudy = getInactiveStudy();

		final ChangeReason changeReason = new ChangeReason();
		changeReason.setChangeReason("Deleted study");
		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO();
		deleteStudyDTO.setChangeReason(changeReason);
		deleteStudyDTO.setStudyId(inactiveStudy.getId());

		assertDoesNotThrow(() -> {
			studyService.deleteStudy(deleteStudyDTO);
		});

		Optional<Study> deletedStudy = studyRepository.findById(inactiveStudy.getId());
		assertFalse(deletedStudy.isPresent(), "Study should have been deleted!");

		var studyAcl = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), null);
		AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				studyAcl, inactiveStudy.getId());
		assertNull(aclObjectIdentity, "AclObjectIdentity for study should have been deleted!");

		var siteAcl = aclClassRepository.findFirstByClassNameOrSynonym(Site.class.getName(), null);
		for (final Site site : inactiveStudy.getSites()) {
			var siteIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(siteAcl,
			                                                                                           site.getId());
			assertNull(siteIdentity, "AclObjectIdentity for site should have been deleted!");
		}

		final List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(inactiveStudy.getId());
		assertEquals(0, auditEntries.size(), "Audit Entries should have been deleted!");
	}

	@Test
	public void deleteTestModeStudyEmpty() {
		final Study testModeStudy = getTestModeStudy();
		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO();
		deleteStudyDTO.setStudyId(testModeStudy.getId());

		assertDoesNotThrow(() -> studyService.deleteStudy(deleteStudyDTO));

		assertFalse(studyRepository.existsById(testModeStudy.getId()), "Study should have been deleted!");
	}

	@Test
	public void deleteTestModeStudyNotEmpty() {
		final Study testModeStudy = getTestModeStudy();
		addSubject(testModeStudy);

		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO();
		deleteStudyDTO.setStudyId(testModeStudy.getId());

		assertDoesNotThrow(() -> studyService.deleteStudy(deleteStudyDTO));

		assertFalse(studyRepository.existsById(testModeStudy.getId()), "Study should have been deleted!");
	}

	@Test
	public void deleteActiveStudy() {
		final Study activeStudy = getActiveStudy();
		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO();
		deleteStudyDTO.setStudyId(activeStudy.getId());

		var exception = assertThrows(RandimiException.class, () -> {
			studyService.deleteStudy(deleteStudyDTO);
		});

		assertEquals(RandimiException.NOT_ACCEPTABLE_STUDY_ACTIVE, exception.getErrorCode(), "Unexpected error code!");
		assertEquals(StudyStatus.ACTIVE, activeStudy.getStatus(), "Study should not have been deleted!");
	}

	@Test
	public void deleteArchivedStudy() {
		final Study inactiveStudy = getArchivedStudy();

		final ChangeReason changeReason = new ChangeReason();
		changeReason.setChangeReason("Deleted study");
		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO();
		deleteStudyDTO.setChangeReason(changeReason);
		deleteStudyDTO.setStudyId(inactiveStudy.getId());

		assertDoesNotThrow(() -> {
			studyService.deleteStudy(deleteStudyDTO);
		});

		Optional<Study> deletedStudy = studyRepository.findById(inactiveStudy.getId());
		assertTrue(deletedStudy.isPresent(), "Study should not have been deleted!");

		// Test ACL
		var studyAcl = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), null);
		AclObjectIdentity studyIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				studyAcl, inactiveStudy.getId());
		assertNotNull(studyIdentity, "AclObjectIdentity for study should not have been deleted!");

		var aclEntriesStudy = aclEntryRepository.findByAclObjectIdentity(studyIdentity);
		assertEquals(0, aclEntriesStudy.size(), "AclEntries for study should have been deleted!");

		var siteAcl = aclClassRepository.findFirstByClassNameOrSynonym(Site.class.getName(), null);
		for (final Site site : inactiveStudy.getSites()) {
			var existsSite = siteRepository.existsById(site.getId());
			assertTrue(existsSite, "Site should not have been deleted!");

			var siteIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(siteAcl,
			                                                                                           site.getId());
			assertNotNull(siteIdentity, "AclObjectIdentity for site should not have been deleted!");

			var aclEntriesSite = aclEntryRepository.findByAclObjectIdentity(siteIdentity);
			assertEquals(0, aclEntriesSite.size(), "AclEntries for study should have been deleted!");
		}

		assertEquals(0, deletedStudy.get().getSubjectLists().size(), "Subject lists should have been deleted!");
		assertEquals(0, deletedStudy.get().getAssignedUsers().size(), "User should have been unassigned from the study!");

		final List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(inactiveStudy.getId());
		assertEquals(4, auditEntries.size(), "Audit Entries should have been deleted!");

		var audit = testLastAuditEntryForStudy(inactiveStudy.getId(), AuditType.DELETE, "Deleted study");
		assertEquals("ADMIN", audit.getUsername(), "Unexpected user in audit entry!");
	}

}
