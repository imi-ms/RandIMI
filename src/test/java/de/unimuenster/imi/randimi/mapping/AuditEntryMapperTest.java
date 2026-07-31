package de.unimuenster.imi.randimi.mapping;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.AuditEntryDTO;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuditEntryMapperTest extends RandimiIntegrationTest {

	@Autowired
	private MessageService messageService;

	private AuditEntryMapper mapper;

	@BeforeEach
	public void setUp() {
		Study study = new Study();
		study.setGuiName("Test Study");

		StudyRepository studyRepository = mock(StudyRepository.class);
		when(studyRepository.findById(any())).thenReturn(Optional.of(study));

		mapper = new AuditEntryMapper(messageService, studyRepository);
	}

	@Test
	public void toAuditEntryDTOCustomReason() {
		AuditEntry auditEntry = new AuditEntry();
		auditEntry.setReason("typo");

		AuditEntryDTO dto = mapper.toAuditEntryDTO(auditEntry);
		assertEquals(auditEntry.getReason(), dto.getReason());
	}

	@Test
	public void toAuditEntryDTOReasonType() {
		AuditEntry auditEntry = new AuditEntry();
		auditEntry.setReason("TYPO");

		AuditEntryDTO dto = mapper.toAuditEntryDTO(auditEntry);
		assertEquals(messageService.getMessage("enum.AuditReasonType." + auditEntry.getReason()), dto.getReason());
	}

}
