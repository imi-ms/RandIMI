package de.unimuenster.imi.randimi.mapping;

import de.unimuenster.imi.randimi.dto.AuditEntryDTO;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.AuditReasonType;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditEntryMapper {

	private final MessageService messageService;

	private final StudyRepository studyRepository;

	@Autowired
	public AuditEntryMapper(final MessageService messageService, final StudyRepository studyRepository) {
		this.messageService = messageService;
		this.studyRepository = studyRepository;
	}

	public AuditEntryDTO toAuditEntryDTO(final AuditEntry auditEntry) {
		AuditEntryDTO auditEntryDTO = new AuditEntryDTO();

		auditEntryDTO.setTimestamp(auditEntry.getTimestamp());
		auditEntryDTO.setUsername(auditEntry.getUsername());
		auditEntryDTO.setStudyName(studyRepository.findById(auditEntry.getStudyId()).get().getGuiName());
		auditEntryDTO.setAuditClass(auditEntry.getAuditClass());
		auditEntryDTO.setAuditType(auditEntry.getAuditType());
		auditEntryDTO.setContent(auditEntry.getContent());
		auditEntryDTO.setOldContent(auditEntry.getOldContent());

		final String reason = auditEntry.getReason();
		if (AuditReasonType.isValid(reason)) {
			auditEntryDTO.setReason(messageService.getMessage("enum.AuditReasonType." + reason));
		} else {
			auditEntryDTO.setReason(reason);
		}

		return auditEntryDTO;
	}
}
