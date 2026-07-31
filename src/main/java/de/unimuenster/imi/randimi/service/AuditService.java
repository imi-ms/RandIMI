package de.unimuenster.imi.randimi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.user.StudyUsersDTO;
import de.unimuenster.imi.randimi.dto.subject.DeleteSubjectDTO;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.audit.ExportSubjectsAuditEntry;
import de.unimuenster.imi.randimi.model.audit.StatusChangeAuditEntry;
import de.unimuenster.imi.randimi.model.audit.SubjectAuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.*;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.repository.AuditEntryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class AuditService {

	private static final Logger LOGGER = LogManager.getLogger(AuditService.class);

	private final ObjectMapper objectMapper;

	private final AuditEntryRepository auditEntryRepository;

	@Autowired
	public AuditService(final ObjectMapper objectMapper, final AuditEntryRepository auditEntryRepository) {
		this.objectMapper = objectMapper;
		this.auditEntryRepository = auditEntryRepository;
	}

	@Nullable
	public String getOldDto(@Nullable final Object object) {
		if (object == null) {
			return null;
		}
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			LOGGER.error("Unable to convert DTO");
			LOGGER.error(e);
			return null;
		}
	}

	//==================================================================================================================
	// Study
	//==================================================================================================================

	@Transactional
	public void createAuditEntryReadStudy(final long studyId) {
		createAuditEntry(AuditClass.STUDY, AuditType.READ, studyId, studyId, null, null, null);
	}

	@Transactional
	public void createAuditEntryReadSubjects(final long studyId) {
		createAuditEntry(AuditClass.STUDY, AuditType.READ_SUBJECTS, studyId, studyId, null, null, null);
	}

	@Transactional
	public void createAuditEntryExportSubjects(final Set<SubjectStatus> status,
	                                           final Set<String> siteApiIds,
	                                           final Map<String, Set<String>> strata,
	                                           final long studyId) {
		final ExportSubjectsAuditEntry audit = new ExportSubjectsAuditEntry(studyId, siteApiIds, status, strata);
		createAuditEntry(AuditClass.STUDY, AuditType.EXPORT_SUBJECTS, studyId, studyId, null, audit, null);
	}

	@Transactional
	public void createAuditEntryCreateStudy(final StudyDTO studyDTO, final long studyId) {
		createAuditEntry(AuditClass.STUDY, AuditType.CREATE, studyId, studyId, null, studyDTO, null);
	}

	@Transactional
	public void createAuditEntryUpdateStudy(final StudyDTO studyDTO, final long studyId,
	                                        final ChangeReason changeReason) {
		createAuditEntry(AuditClass.STUDY, AuditType.UPDATE, studyId, studyId, changeReason.getChangeReason(), studyDTO,
		                 changeReason.getOldDto());
	}

	@Transactional
	public void createAuditEntryDeleteStudy(final StudyDTO studyDTO, final long studyId,
	                                        final ChangeReason changeReason) {
		createAuditEntry(AuditClass.STUDY, AuditType.DELETE, studyId, studyId, changeReason.getChangeReason(), null,
		                 studyDTO);
	}

	@Transactional
	public void createAuditEntryStudyStatusChange(final long studyId, final AuditType auditType,
	                                              final StatusChangeAuditEntry oldStatus, final StatusChangeAuditEntry newStatus) {
		createAuditEntry(AuditClass.STUDY, auditType, studyId, studyId, null, newStatus, oldStatus);
	}

	@Transactional
	public void createAuditEntryUsernameChange(final long studyId, final long userId, final String oldUsername,
	                                           final String newUsername) {
		createAuditEntry(AuditClass.RANDIMI_USER, AuditType.USERNAME_CHANGE, studyId, userId, null, newUsername,
		                 oldUsername);
	}

	//==================================================================================================================
	// StudyUsers
	//==================================================================================================================

	@Transactional
	public void createAuditEntryUpdateStudyUsers(final StudyUsersDTO studyUsersDTO, final ChangeReason changeReason) {
		createAuditEntry(AuditClass.STUDY, AuditType.UPDATE, studyUsersDTO.getStudyId(), studyUsersDTO.getStudyId(),
		                 changeReason.getChangeReason(), studyUsersDTO, changeReason.getOldDto());
	}

	//==================================================================================================================
	// Subject
	//==================================================================================================================

	@Transactional
	public void createAuditEntryCreateSubject(final Subject subject) {
		final SubjectAuditEntry dto = createSubjectAuditEntry(subject);
		createAuditEntry(AuditClass.SUBJECT, AuditType.CREATE, subject.getSubjectList().getStudy().getId(),
		                 subject.getId(), null, dto, null);
	}

	@Transactional
	public void createAuditEntryDeleteOrReleaseSubject(final boolean release, final String changeReason,
	                                                   final long studyId, final long subjectId,
	                                                   final SubjectAuditEntry oldDto,
	                                                   final SubjectAuditEntry newDto) {
		final AuditType auditType = release ? AuditType.RELEASE_SUBJECT : AuditType.DELETE;
		createAuditEntry(AuditClass.SUBJECT, auditType, studyId, subjectId, changeReason, newDto, oldDto);
	}

	@Transactional
	public void createAuditEntryUpdatePseudonym(final String changeReason, final long studyId, final long subjectId,
	                                             final SubjectAuditEntry oldDto, final SubjectAuditEntry newDto) {
		createAuditEntry(AuditClass.SUBJECT, AuditType.UPDATE, studyId, subjectId, changeReason, newDto, oldDto);
	}

	//==================================================================================================================
	// Delete
	//==================================================================================================================

	@Transactional
	public void deleteAuditForStudy(final long studyId) {
		auditEntryRepository.deleteByStudyId(studyId);
	}

	@Transactional
	public void deleteAuditForStudyAndAuditClass(final long studyId, final AuditClass auditClass) {
		auditEntryRepository.deleteByStudyIdAndAuditClass(studyId, auditClass);
	}

	//==================================================================================================================
	// Helper SubjectAuditEntry creation
	//==================================================================================================================

	/**
	 * Creates a SubjectAuditEntry based on the given subject.
	 * @param subject Subject to convert.
	 * @return The created SubjectAuditEntry.
	 */
	public SubjectAuditEntry createSubjectAuditEntry(final Subject subject) {
		final List<String> parts = new ArrayList<>();
		for (final var p : subject.getSubjectList().getStratumParts()) {
			if (p.getStratum().getStratumType() == StratumType.ENUM) {
				parts.add(p.getPartKey());
			}
		}

		final String[] enumeratedStrata = parts.toArray(new String[0]);

		return new SubjectAuditEntry(subject.getPseudonym(), subject.getSite().getGuiName(), enumeratedStrata,
		                             subject.getStudyArm(), subject.getStatus().toString());
	}

	//==================================================================================================================
	// Helper StatusChangeAuditEntry creation
	//==================================================================================================================

	/**
	 * Creates a StatusChangeAuditEntry based on the given study.
	 * @param study Study to convert.
	 * @return The created StatusChangeAuditEntry.
	 */
	public StatusChangeAuditEntry createStatusChangeAuditEntry(final Study study) {
		var statusChangeNew = new StatusChangeAuditEntry();
		statusChangeNew.setStatus(study.getStatus());
		statusChangeNew.setRetentionPeriod(study.getRetentionPeriod() == null
		                                   ? null
		                                   : study.getRetentionPeriod().toLocalDateTime().toLocalDate());
		return statusChangeNew;
	}

	//==================================================================================================================
	// Helper fill
	//==================================================================================================================

	private void createAuditEntry(final AuditClass auditClass, final AuditType auditType, final long studyId,
	                              final long targetId, @Nullable final String reason, @Nullable final Object dto,
	                              @Nullable final Object oldDTO) {
		final String oldDTOString = getOldDto(oldDTO);
		createAuditEntry(auditClass, auditType, studyId, targetId, reason, dto, oldDTOString);
	}

	private void createAuditEntry(final AuditClass auditClass, final AuditType auditType, final long studyId,
	                              final long targetId, @Nullable final String reason, @Nullable final Object dto,
	                              @Nullable final String oldDto) {
		final AuditEntry auditEntry = new AuditEntry();

		auditEntry.setStudyId(studyId);
		auditEntry.setTargetId(targetId);
		auditEntry.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		auditEntry.setAuditType(auditType);
		auditEntry.setAuditClass(auditClass);
		auditEntry.setReason(reason);
		auditEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));

		if (auditEntry.getAuditType() == AuditType.CREATE ||
		    auditEntry.getAuditType() == AuditType.EXPORT_SUBJECTS) {
			setCreateContent(auditEntry, dto);
		} else if (auditEntry.getAuditType() == AuditType.UPDATE ||
		           auditEntry.getAuditType() == AuditType.ACTIVATE ||
                   auditEntry.getAuditType() == AuditType.TEST ||
                   auditEntry.getAuditType() == AuditType.LOCK ||
                   auditEntry.getAuditType() == AuditType.UNLOCK ||
		           auditEntry.getAuditType() == AuditType.USERNAME_CHANGE ||
		           auditEntry.getAuditType() == AuditType.RELEASE_SUBJECT ||
		           auditEntry.getAuditType() == AuditType.ARCHIVE ||
		           auditEntry.getAuditType() == AuditType.REACTIVATE) {
			setUpdateContent(auditEntry, dto, oldDto);
		} else if (auditEntry.getAuditType() == AuditType.DELETE) {
			setDeleteContent(auditEntry, oldDto);
		}

		auditEntryRepository.save(auditEntry);
	}

	private void setCreateContent(final AuditEntry auditEntry, @Nullable final Object dto) {
		setContent(auditEntry, dto);
		auditEntry.setOldContent("");
	}

	private void setDeleteContent(final AuditEntry auditEntry, @Nullable final String oldDto) {
		auditEntry.setOldContent("");
		if (oldDto == null) {
			LOGGER.error("Unable to set content of audit entry. Old DTO data is null");
		} else {
			auditEntry.setOldContent(oldDto);
		}
	}

	private void setContent(final AuditEntry auditEntry, @Nullable final Object dto) {
		auditEntry.setContent("");

		if (dto == null) {
			LOGGER.error("Unable to set content of audit entry. Object is null");
			return;
		}

		try {
			auditEntry.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto));
		} catch (JsonProcessingException e) {
			LOGGER.error("Unable to set content of audit entry. Failed to convert JsonNode", e);
		}
	}

	private void setUpdateContent(final AuditEntry auditEntry, @Nullable final Object dto,
	                              @Nullable final String oldDto) {
		if (dto == null) {
			LOGGER.error("Unable to set content of audit entry. Object is null");
			return;
		}
		if (oldDto == null) {
			LOGGER.error("Unable to set content of audit entry. Old DTO data is null");
			return;
		}

		final JsonNode dtoJsonNode = objectMapper.valueToTree(dto);
		if (dtoJsonNode instanceof ObjectNode dtoNode) {
			final ObjectNode oldDtoNode;
			try {
				oldDtoNode = (ObjectNode) objectMapper.readTree(oldDto);
			} catch (JsonProcessingException e) {
				LOGGER.error("Unable to set content of audit entry. Failed to parse old DTO data", e);
				return;
			}

			final var differences = findDifferences(oldDtoNode, dtoNode);

			if (differences != null) {
				auditEntry.setContent("");
				auditEntry.setOldContent("");

				try {
					auditEntry.setOldContent(objectMapper.writerWithDefaultPrettyPrinter()
					                                     .writeValueAsString(differences.getFirst()));
				} catch (JsonProcessingException e) {
					LOGGER.error("Unable to set old content of audit entry. Failed to convert JsonNode", e);
				}
				try {
					auditEntry.setContent(objectMapper.writerWithDefaultPrettyPrinter()
					                                  .writeValueAsString(differences.getSecond()));
				} catch (JsonProcessingException e) {
					LOGGER.error("Unable to set content of audit entry. Failed to convert JsonNode", e);
				}
			}

		} else {
			auditEntry.setOldContent(oldDto);
			auditEntry.setContent(dtoJsonNode.textValue());
		}

	}

	@Nullable
	private <T extends JsonNode> Pair<T, T> findDifferences(final T oldNode, final T newNode) {
		// Compare strings to prevent differences because of different node types
		if (oldNode.toString().equals(newNode.toString())) {
			return null;
		}

		final T oldDifferences;
		final T newDifferences;

		if (oldNode.isObject()) {
			final ObjectNode oldObjectDifferences = objectMapper.createObjectNode();
			final ObjectNode newObjectDifferences = objectMapper.createObjectNode();

			final Set<Map.Entry<String, JsonNode>> properties = oldNode.properties();
			for (final Map.Entry<String, JsonNode> entry : properties) {
				final var oldChild = entry.getValue();
				final var newChild = newNode.get(entry.getKey());

				final var result = findDifferences(oldChild, newChild);

				if (result != null) {
					oldObjectDifferences.set(entry.getKey(), result.getFirst());
					newObjectDifferences.set(entry.getKey(), result.getSecond());
				}
			}

			oldDifferences = (T) oldObjectDifferences;
			newDifferences = (T) newObjectDifferences;
		} else if (oldNode.isArray()) {
			final var oldArrayNode = oldNode.isEmpty() ? objectMapper.createArrayNode() : (ArrayNode) oldNode;
			final var newArrayNode = newNode.isEmpty() ? objectMapper.createArrayNode() : (ArrayNode) newNode;

			final var oldArrayDifferences = objectMapper.createArrayNode();
			final var newArrayDifferences = objectMapper.createArrayNode();

			final int size = Math.max(oldArrayNode.size(), newArrayNode.size());

			for (int i = 0; i < size; ++i) {
				if (oldArrayNode.size() <= i) {
					newArrayDifferences.add(newArrayNode.get(i));
				} else if (newArrayNode.size() <= i) {
					oldArrayDifferences.add(oldArrayNode.get(i));
				} else {
					final var result = findDifferences(oldArrayNode.get(i), newArrayNode.get(i));

					if (result != null) {
						oldArrayDifferences.add(result.getFirst());
						newArrayDifferences.add(result.getSecond());
					}
				}
			}

			oldDifferences = (T) oldArrayDifferences;
			newDifferences = (T) newArrayDifferences;
		} else {
			oldDifferences = oldNode;
			newDifferences = newNode;
		}

		return Pair.of(oldDifferences, newDifferences);
	}
}
