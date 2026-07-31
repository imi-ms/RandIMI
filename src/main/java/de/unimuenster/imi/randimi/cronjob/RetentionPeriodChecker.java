package de.unimuenster.imi.randimi.cronjob;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.DeleteStudyDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.service.AuditService;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandimiMailService;
import de.unimuenster.imi.randimi.service.StudyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/**
 * Cronjob that checks the retention period of each study and sends reminders or deletes studies
 *
 * @author Daniel Preciado-Marquez
 * */
@Slf4j @Service
public class RetentionPeriodChecker {

	private final StudyRepository studyRepository;
	private final AuditService auditService;
	private final MessageService messageService;
	private final RandimiMailService mailService;
	private final StudyService studyService;

	@Autowired
	public RetentionPeriodChecker(final StudyRepository studyRepository, final AuditService auditService,
	                              final MessageService messageService, final RandimiMailService mailService,
	                              final StudyService studyService) {
		this.studyRepository = studyRepository;
		this.auditService = auditService;
		this.messageService = messageService;
		this.mailService = mailService;
		this.studyService = studyService;
	}

	@WithSystemUser
	@Scheduled(cron = "0 5 2 * * ?")
	public void remindRetentionPeriod() {
		final var now = LocalDate.now();

		final var firstWarning = now.plusMonths(3);
		final var firstWarningTimestamp = Timestamp.valueOf(firstWarning.atStartOfDay());
		sendReminder(studyRepository.findByRetentionPeriodIs(firstWarningTimestamp));

		final var secondWarning = now.plusMonths(1);
		final var secondWarningTimestamp = Timestamp.valueOf(secondWarning.atStartOfDay());
		sendReminder(studyRepository.findByRetentionPeriodIs(secondWarningTimestamp));

		final var thirdWarning = now.plusWeeks(1);
		final var thirdWarningTimestamp = Timestamp.valueOf(thirdWarning.atStartOfDay());
		sendReminder(studyRepository.findByRetentionPeriodIs(thirdWarningTimestamp));

		final var nowTimestamp = Timestamp.valueOf(now.atStartOfDay());
		sendReminder(studyRepository.findByRetentionPeriodIs(nowTimestamp));

		final var delete = now.minusDays(1);
		final var deleteTimestamp = Timestamp.valueOf(delete.atStartOfDay());
		deleteStudies(studyRepository.findByRetentionPeriodIs(deleteTimestamp));
	}

	private void sendReminder(final List<Study> studies) {
		for (final var study : studies) {
			final var subject = messageService.getMessage("mail.retentionPeriod.reminder.subject", study.getGuiName(),
			                                              study.getRetentionPeriod());
			final var content = mailService.assembleMailText("mail.retentionPeriod.reminder.content",
			                                                 new Object[]{study.getGuiName(),
			                                                              study.getRetentionPeriod()});
			mailService.sendStudyNotification(study, subject, content);
		}
	}

	private void deleteStudies(final List<Study> studies) {
		final String reason = messageService.getMessage("enum.AuditReasonType.RETENTION_PERIOD");

		for (final var study : studies) {
			// Send notification mail
			final var subject = messageService.getMessage("mail.retentionPeriod.expired.subject", study.getGuiName());
			final var content = mailService.assembleMailText("mail.retentionPeriod.expired.content",
			                                                 new Object[]{study.getGuiName()});
			mailService.sendStudyNotification(study, subject, content);

			// Delete
			final String oldDto = auditService.getOldDto(study);
			try {
				studyService.deleteStudy(new DeleteStudyDTO(new ChangeReason(reason, oldDto), study.getId()));
			} catch (final RandimiException e) {
				log.error("Failed to delete study with expired retention period", e);
			}
		}

	}

}
