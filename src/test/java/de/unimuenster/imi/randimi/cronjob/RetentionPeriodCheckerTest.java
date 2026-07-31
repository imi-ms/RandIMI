package de.unimuenster.imi.randimi.cronjob;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.service.RandimiMailService;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@WithUserDetails(value = "admin", userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
class RetentionPeriodCheckerTest extends RandimiIntegrationTest {

	@Autowired private RandimiMailService mailService;

	@Autowired private RetentionPeriodChecker retentionPeriodChecker;

	private GreenMail greenMail;

	@BeforeEach
	public void setUp() {
		mailService.configureMailSender("localhost", 3025, "", "", false, false);

		var greenMailServerSetup = new ServerSetup(3025, null, "smtp");
		this.greenMail = new GreenMail(greenMailServerSetup);
		this.greenMail.start();
	}

	@AfterEach
	public void tearDown() {
		this.greenMail.stop();
	}

	@Test
	void remindFirst() throws MessagingException, IOException {
		var retentionPeriod = LocalDate.now();
		retentionPeriod = retentionPeriod.plusMonths(3);
		var archivedStudy = getArchivedStudy(retentionPeriod);
		var retentionPeriodString = retentionPeriod.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", LocaleContextHolder.getLocale()));

		var user = getActiveUser();

		retentionPeriodChecker.remindRetentionPeriod();

		greenMail.waitForIncomingEmail(1);
		var receivedMessages = greenMail.getReceivedMessages();

		assertEquals(1, receivedMessages.length, "Unexpected number of messages!");

		var reminder = receivedMessages[0];

		var recipients = reminder.getAllRecipients();
		assertEquals(1, recipients.length, "Unexpected number of recipients!");
		assertEquals(user.getEMail(), recipients[0].toString(), "Unexpected user email!");

		var subject = reminder.getSubject();
		var expectedSubject = "Die Aufbewahrungsfrist der Studie „" + archivedStudy.getGuiName() + "“ läuft am " + retentionPeriodString + " ab";
		assertEquals(expectedSubject, subject, "Unexpected subject!");

		var body = ((String) reminder.getContent()).replaceAll("\r\n", "\n");
		var expectedBody = """
				Sehr geehrte*r Nutzer*in,
				
				die Aufbewahrungsfrist der Studie „""" + archivedStudy.getGuiName() + "“ läuft am " + retentionPeriodString + """
				 ab.
				Nach Ablauf wird die Studie automatisch gelöscht.
				Falls die Studie noch weiterhin benötigt wird, können Sie die Aufbewahrungsfrist anpassen oder die Studie exportieren.
			
				Ihr RandIMI-Team
			
				--\s
				RandIMI
				E-Mail: randimi@uni-muenster.de
				Tel.: +49 (0)251 83 52526""";
		assertEquals(expectedBody, body, "Unexpected body!");
	}

	@Test
	void remindFirstEnglish() throws MessagingException, IOException {
		LocaleContextHolder.setDefaultLocale(SupportedLanguage.ENGLISH.toLocale());

		var retentionPeriod = LocalDate.now();
		retentionPeriod = retentionPeriod.plusMonths(3);
		var archivedStudy = getArchivedStudy(retentionPeriod);
		var retentionPeriodString = retentionPeriod.format(
				DateTimeFormatter.ofPattern("MMM d, yyyy", LocaleContextHolder.getLocale()));

		var user = getActiveUser();

		retentionPeriodChecker.remindRetentionPeriod();

		greenMail.waitForIncomingEmail(1);
		var receivedMessages = greenMail.getReceivedMessages();

		assertEquals(1, receivedMessages.length, "Unexpected number of messages!");

		var reminder = receivedMessages[0];

		var recipients = reminder.getAllRecipients();
		assertEquals(1, recipients.length, "Unexpected number of recipients!");
		assertEquals(user.getEMail(), recipients[0].toString(), "Unexpected user email!");


		var subject = reminder.getSubject();
		var expectedSubject = "The retention period of the study “" + archivedStudy.getGuiName() + "” expires on " + retentionPeriodString;
		assertEquals(expectedSubject, subject, "Unexpected subject!");

		var body = ((String) reminder.getContent()).replaceAll("\r\n", "\n");
		var expectedBody = """
				Dear user,
				
				the retention period of the study “%s” expires on %s.
				The study is automatically deleted after expiry.
				If the study is still required, you can adjust the retention period.
				
				Your RandIMI team
				
				--\s
				RandIMI
				email: randimi@uni-muenster.de
				tel.: +49 (0)251 83 52526""".formatted(archivedStudy.getGuiName(), retentionPeriodString);
		assertEquals(expectedBody, body, "Unexpected body!");
	}

	@Test
	void delete() throws MessagingException, IOException {
		var retentionPeriod = LocalDate.now();
		retentionPeriod = retentionPeriod.minusDays(1);
		var archivedStudy = getArchivedStudy(retentionPeriod);

		var user = getActiveUser();

		retentionPeriodChecker.remindRetentionPeriod();

		greenMail.waitForIncomingEmail(1);
		var receivedMessages = greenMail.getReceivedMessages();

		assertEquals(1, receivedMessages.length, "Unexpected number of messages!");

		var reminder = receivedMessages[0];

		var recipients = reminder.getAllRecipients();
		assertEquals(1, recipients.length, "Unexpected number of recipients!");
		assertEquals(user.getEMail(), recipients[0].toString(), "Unexpected user email!");

		var subject = reminder.getSubject();
		var expectedSubject = "Löschung der Studie „" + archivedStudy.getGuiName() + "“";
		assertEquals(expectedSubject, subject, "Unexpected subject!");

		var body = ((String) reminder.getContent()).replaceAll("\r\n", "\n");
		var expectedBody = """
				Sehr geehrte*r Nutzer*in,
				
				aufgrund der abgelaufenen Aufbewahrungsfrist wurde die Studie „""" + archivedStudy.getGuiName() + """
				“ automatisch gelöscht.
			
				Ihr RandIMI-Team
			
				--\s
				RandIMI
				E-Mail: randimi@uni-muenster.de
				Tel.: +49 (0)251 83 52526""";
		assertEquals(expectedBody, body, "Unexpected body!");

		assertEquals(StudyStatus.DELETED, archivedStudy.getStatus(), "Study does not have been deleted!");
		var audit = testLastAuditEntryForStudy(archivedStudy.getId(), AuditType.DELETE, "Aufbewahrungsfrist abgelaufen");
		assertEquals(WithSystemUserAspect.SYSTEM_USERNAME, audit.getUsername(), "Unexpected username of audit entry!");
	}

}