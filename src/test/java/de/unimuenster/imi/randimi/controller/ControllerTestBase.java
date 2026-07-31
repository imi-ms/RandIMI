package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.AuditClass;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Daniel Preciado-Marquez
 */
public abstract class ControllerTestBase extends RandimiIntegrationTest {

	protected void testLastAuditEntryForSubject(Long studyId, AuditType expectedAuditType, String expectedReason,
	                                            Long expectedTargetId) {
		List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(studyId);
		auditEntries.sort(Comparator.comparing(AuditEntry::getTimestamp));
		AuditEntry lastAuditEntry = auditEntries.get(auditEntries.size() - 1);

		assertEquals(AuditClass.SUBJECT, lastAuditEntry.getAuditClass(), "AuditClass is of the wrong type!");
		assertEquals(expectedAuditType, lastAuditEntry.getAuditType(), "Last AuditEntry is of the wrong type!");
		assertEquals(expectedReason, lastAuditEntry.getReason(), "Last AuditEntry has an unexpected change reason");
		assertEquals(expectedTargetId, lastAuditEntry.getTargetId(), "Last AuditEntry has an unexpected targetId");
	}
}
