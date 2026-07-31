package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WithUserDetails(value = "admin", userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
public class SubjectServiceTest extends RandimiIntegrationTest {

	@Autowired
	SubjectService subjectService;

	@Autowired
	SiteRepository siteRepository;

	/**
	 * ===========================================================================
	 * --- Tests for {@link SubjectService#updatePseudonym(Subject, String, String)} ---
	 * ===========================================================================
	 */

	@Test
	public void updatePseudonymSuccess() {
		final Study activeStudy = getActiveStudy();
		final Subject subject = activeStudy.getSubjectLists().get(0).getSubjects().get(0);
		final String oldPseudonym = subject.getPseudonym();
		final String newPseudonym = "updated-pseudonym-test";

		assertDoesNotThrow(() -> subjectService.updatePseudonym(subject, newPseudonym, "Test reason"));
		assertEquals(newPseudonym, subject.getPseudonym(), "Pseudonym should have been updated!");
		assertNotEquals(oldPseudonym, subject.getPseudonym(), "Pseudonym should have changed!");

		List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(activeStudy.getId());
		auditEntries.sort(Comparator.comparing(AuditEntry::getTimestamp));
		AuditEntry lastEntry = auditEntries.get(auditEntries.size() - 1);
		assertEquals(AuditType.UPDATE, lastEntry.getAuditType(), "Last audit entry should be UPDATE!");
		assertEquals("Test reason", lastEntry.getReason(), "Audit reason should match!");
		assertEquals(subject.getId(), lastEntry.getTargetId(), "Audit target ID should match!");
	}

	@Test
	public void updatePseudonymSameValue() {
		final Study activeStudy = getActiveStudy();
		final Subject subject = activeStudy.getSubjectLists().get(0).getSubjects().get(0);
		final String currentPseudonym = subject.getPseudonym();

		assertDoesNotThrow(() -> subjectService.updatePseudonym(subject, currentPseudonym, "Test reason"));
		assertEquals(currentPseudonym, subject.getPseudonym(), "Pseudonym should remain the same!");
	}

	@Test
	public void updatePseudonymDuplicateInLocation() {
		final Study activeStudy = getActiveStudy();

		addSubject(activeStudy);

		final List<Subject> subjects = activeStudy.getSubjectLists().get(0).getSubjects();
		final Subject subject1 = subjects.get(0);
		final Subject subject2 = subjects.get(2);
		final String pseudonymToDuplicate = subject1.getPseudonym();

		RandimiException exception = assertThrows(RandimiException.class,
		                                          () -> subjectService.updatePseudonym(subject2, pseudonymToDuplicate, "reason"));
		assertEquals(RandimiException.DUPLICATE_REQUEST_PSEUDONYM_ALREADY_REGISTERED, exception.getErrorCode(),
		             "Unexpected error code!");
		assertNotEquals(pseudonymToDuplicate, subject2.getPseudonym(),
		                "Pseudonym should not have been updated to a duplicate value!");
	}

	@Test
	public void updatePseudonymRegexMismatch() {
		final Study activeStudy = getActiveStudy();
		final Subject subject = activeStudy.getSubjectLists().get(0).getSubjects().get(0);
		final Site site = subject.getSite();

		site.setPseudonymRegex("^[A-Z]{3}-\\d{3}$");
		siteRepository.save(site);

		RandimiException exception = assertThrows(RandimiException.class,
		                                          () -> subjectService.updatePseudonym(subject, "invalid-pseudonym", "reason"));
		assertEquals(RandimiException.UNSATISFYING_PARAMETER_PSEUDONYM_REGEX_MISMATCH, exception.getErrorCode(),
		             "Unexpected error code!");
	}
}
