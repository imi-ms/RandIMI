package de.unimuenster.imi.randimi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import de.unimuenster.imi.randimi.model.AuditEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Daniel Preciado-Marquez
 */
public class AuditEntryRepositoryTest extends RepositoryTestBase {

	@Autowired
	AuditEntryRepository auditEntryRepository;

	@Test
	public void getEntriesForStudyTest() {
		List<AuditEntry> auditEntriesActiveStudy = auditEntryRepository.findByStudyId(activeStudy.getId());
		assertEquals(2, auditEntriesActiveStudy.size());

		List<AuditEntry> auditEntriesInactiveStudy = auditEntryRepository.findByStudyId(inactiveStudy.getId());
		assertEquals(1, auditEntriesInactiveStudy.size());
	}
}
