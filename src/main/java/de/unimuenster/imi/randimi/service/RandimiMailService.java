package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.model.settings.Settings;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.settings.SettingsRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Service
@Log4j2
public class RandimiMailService {

	private final Environment environment;
	private final JavaMailSenderImpl mailSender;
	private final MessageService messageService;
	private final RandimiUserRepository userRepository;
	private final SettingsRepository settingsRepository;

	@Autowired
	public RandimiMailService(final Environment environment, final MessageService messageService,
	                          final RandimiUserRepository userRepository,
	                          final SettingsRepository settingsRepository) {
		this.environment = environment;
		this.messageService = messageService;
		this.userRepository = userRepository;
		this.settingsRepository = settingsRepository;
		mailSender = new JavaMailSenderImpl();
		configureMailSender();
	}

	public void sendSimpleMessageOrThrow(final String to, final String subject,
	                                     final String text) throws MailException {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		message.setFrom(settingsRepository.getMailSender());

		mailSender.send(message);
	}

	public boolean sendSimpleMessage(final String to, final String subject, final String text) {
		try {
			sendSimpleMessageOrThrow(to, subject, text);
		} catch (MailException ex) {
			log.warn("Failed to send E-Mail!", ex);
			return false;
		}
		return true;
	}

	public void sendStudyNotification(final Study study, final String subject, final String content) {
		for (RandimiUser user : userRepository.getNotifiedUsersOfStudy(study)) {
			sendSimpleMessage(user.getEMail(), subject, content);
		}
	}

	public String assembleMailSubject(final String subjectCode) {
		return messageService.getMessage(subjectCode);
	}

	public String assembleMailText(final String contentCode, final Object[] contentArgs) {
		final String header = messageService.getMessage("mail.header");
		final String content = messageService.getMessage(contentCode, contentArgs);
		final String footer = messageService.getMessage("mail.footer", settingsRepository.getSupportMail(),
		                                                settingsRepository.getSupportPhone());

		return header + content + footer;
	}

	public String assembleMailText(final String contentCode) {
		return assembleMailText(contentCode, new Object[]{});
	}

	public void configureMailSender(final Settings settings) {
		configureMailSender(settings.getMailHost(), settings.getMailPort(), settings.getMailUsername(), settings.getMailPassword(), settings.isMailSMTPAuth(), settings.isMailTLS());
	}

	public void configureMailSender() {
		configureMailSender(settingsRepository.getCurrentSettings());
	}

	public void configureMailSender(final String mailHost, final int mailPort, final String mailUsername,
	                                 final String mailPassword, final boolean mailSMTPAuth, final boolean mailTLS) {
		mailSender.setHost(mailHost);
		mailSender.setPort(mailPort);

		if (mailSMTPAuth) {
			mailSender.setUsername(mailUsername);
			mailSender.setPassword(mailPassword);
		} else {
			mailSender.setUsername("");
			mailSender.setPassword("");
		}

		Properties props = mailSender.getJavaMailProperties();

		if (Arrays.asList(environment.getActiveProfiles()).contains("dev"))
			props.put("mail.debug", "true");

		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", mailSMTPAuth);
		props.put("mail.smtp.starttls.enable", mailTLS);
	}
}
