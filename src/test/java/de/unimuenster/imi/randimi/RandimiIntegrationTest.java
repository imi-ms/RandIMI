package de.unimuenster.imi.randimi;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.DeleteStudyDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.AuditClass;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.AuditEntryRepository;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.RandomizationService;
import de.unimuenster.imi.randimi.service.StudyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@Transactional
public abstract class RandimiIntegrationTest extends RandimiTest {

	protected static final String ACTIVE_USER_NAME = "ACTIVE_TEST_USER";
	protected static final String LOCAL_MANAGER_NAME = "TEST_LOCAL_MANAGER";

	@Autowired protected AuditEntryRepository auditEntryRepository;
	@Autowired private RandimiUserRepository randimiUserRepository;
	@Autowired protected StudyRepository studyRepository;

	@Autowired private RandomizationService randomizationService;
	@Autowired private StudyService studyService;

	@BeforeAll
	public static void beforeClass() {
		LocaleContextHolder.setDefaultLocale(SupportedLanguage.GERMAN.toLocale());
	}

	public RandimiUser getActiveUser() {
		return randimiUserRepository.findFirstByUsernameIgnoreCase(ACTIVE_USER_NAME);
	}

	public RandimiUser getLocalManager() {
		return randimiUserRepository.findFirstByUsernameIgnoreCase(LOCAL_MANAGER_NAME);
	}

	protected Study getActiveStudy() {
		return studyRepository.findByGuiName("Active Study").get(0);
	}

	protected Study getInactiveStudy() {
		return studyRepository.findByGuiName("Inactive Study").get(0);
	}

	protected Study getPregeneratedStudy() {
		return studyRepository.findByGuiName("Pre-Generated Study").get(0);
	}

	protected Study getTestModeStudy() {
		final Study study = getInactiveStudy();
		try {
			studyService.changeToTestMode(study);
		} catch (final RandimiException e) {
			fail(e);
		}
		return study;
	}

	protected Study getLockedActiveStudy() {
		final Study study = getActiveStudy();
		try {
			studyService.lockStudy(study);
		} catch (final RandimiException e) {
			fail(e);
		}
		return study;
	}

	protected Study getArchivedStudy() {
		return getArchivedStudy(null);
	}

	protected Study getArchivedStudy(@Nullable final LocalDate retentionPeriod) {
		final Study study = getActiveStudy();
		try {
			studyService.archiveStudy(study, retentionPeriod);
		} catch (RandimiException e) {
			fail(e);
		}
		return study;
	}

	protected Study getDeletedStudy() {
		final Study study = getArchivedStudy();

		var dto = new DeleteStudyDTO(new ChangeReason(), study.getId());
		try {
			studyService.deleteStudy(dto);
		} catch (final RandimiException e) {
			fail(e);
		}

		return study;
	}

	protected void addSubject(final Study study) {
		final SubjectDTO subject = new SubjectDTO();
		subject.setPseudonym("TestSubject");
		subject.setStudyId(study.getId());
		subject.setSiteId(study.getSites().get(0).getId());

		String[] stratumValues = new String[study.getStratums().size()];
		for (int i = 0; i < stratumValues.length; i++) {
			stratumValues[i] = study.getStratums().get(i).getStratumParts().get(0).getPartKey();
		}
		subject.setEnumeratedStratums(stratumValues);

		assertDoesNotThrow(() -> randomizationService.assignSubjectToStudyArm(subject));
	}

	protected AuditEntry testLastAuditEntryForStudy(Long studyId, AuditType expectedAuditType, String expectedReason) {
		List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(studyId);
		auditEntries.sort(Comparator.comparing(AuditEntry::getTimestamp));
		AuditEntry lastAuditEntry = auditEntries.get(auditEntries.size() - 1);

		Assertions.assertEquals(AuditClass.STUDY, lastAuditEntry.getAuditClass(), "AuditClass is of the wrong type!");
		assertEquals(expectedAuditType, lastAuditEntry.getAuditType(), "Last Audit Entry is of the wrong type!");
		assertEquals(expectedReason, lastAuditEntry.getReason());

		return lastAuditEntry;
	}

	protected void testLastAuditEntryForStudy(Long studyId, AuditType expectedAuditType, String expectedReason,
	                                          String expectedContent, String expectedOldContent) {
		var lastAuditEntry = testLastAuditEntryForStudy(studyId, expectedAuditType, expectedReason);
		assertEquals(expectedContent,lastAuditEntry.getContent() , "Unexpected content of last AuditEntry!");
		assertEquals(expectedOldContent, lastAuditEntry.getOldContent(), "Unexpected old content of last AuditEntry!");
	}
}
