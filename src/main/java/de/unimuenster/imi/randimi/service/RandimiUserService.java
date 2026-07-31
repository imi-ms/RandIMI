package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.mapping.user.AccountDetailsMapper;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.ForgotPasswordTokenRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Service class for RandimiUsers.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class RandimiUserService {

	/**
	 * Time an invitation expires after the inviting the user in ms.
	 * The invitation expires after one week.
	 */
	public static final long INVITATION_EXPIRATION_THRESHOLD = 604_800_000;

	/**
	 * Time a ForgotPasswordToken is valid in milliseconds.
	 */
	public static final long FORGOT_PASSWORD_TOKEN_EXPIRATION_THRESHOLD = 3_600_000;

	private final SessionRegistry sessionRegistry;
	private final AclEntryRepository aclEntryRepository;
	private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;
	private final RandimiUserRepository randimiUserRepository;
	private final StudyRepository studyRepository;
	private final RandimiMailService mailService;
	private final AuditService auditService;
	private final AccountDetailsMapper accountDetailsMapper;


	@Autowired
	public RandimiUserService(SessionRegistry sessionRegistry, AclEntryRepository aclEntryRepository,
	                          ForgotPasswordTokenRepository forgotPasswordTokenRepository,
	                          RandimiUserRepository randimiUserRepository, StudyRepository studyRepository,
	                          RandimiMailService mailService, AuditService auditService,
	                          AccountDetailsMapper accountDetailsMapper) {
		this.sessionRegistry = sessionRegistry;
		this.aclEntryRepository = aclEntryRepository;
		this.forgotPasswordTokenRepository = forgotPasswordTokenRepository;
		this.randimiUserRepository = randimiUserRepository;
		this.studyRepository = studyRepository;
		this.mailService = mailService;
		this.auditService = auditService;
		this.accountDetailsMapper = accountDetailsMapper;
	}

	/**
	 * Creates an invitation for the given user and sends the invitation E-Mail.
	 * @param user The user to be invited.
	 * @param request Request for creating the invitation link.
	 * @return If the invitation E-Mail has been sent successfully.
	 */
	public boolean inviteUser(RandimiUser user, HttpServletRequest request) {
		user.setInvitationToken(createInvitationToken());
		user.setInvitationTimestamp(new Timestamp(System.currentTimeMillis()));
		String invitationLink = assembleInvitationLink(request, user);

		final String subject = mailService.assembleMailSubject("invitation.mail.subject");
		final String content = mailService.assembleMailText("invitation.mail.content", new Object[]{invitationLink});

		return mailService.sendSimpleMessage(user.getEMail(), subject, content);
	}

	/**
	 * Updated the account details of the corresponding user.
	 * If the given username is different from the current one,
	 * an audit entry documenting the name change is created for each study the user has access to.
	 * Updates the current security context with the new account details.
	 *
	 * @param accountDetails New account details of the user.
	 */
	public void updateUser(final AccountDetailsDTO accountDetails) {
		final RandimiUser originalUser = randimiUserRepository.findById(accountDetails.getId()).get();
		final String originalUserName = originalUser.getUsername();
		final RandimiUser updateUser = accountDetailsMapper.toRandimiUser(accountDetails, originalUser);

		if (!originalUserName.equals(updateUser.getUsername())) {
			for (final Study study : originalUser.getAssignedStudies()) {
				auditService.createAuditEntryUsernameChange(study.getId(), updateUser.getId(), originalUserName,
				                                            updateUser.getUsername());
			}
		}

		randimiUserRepository.save(updateUser);

		MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
		                                                                 .getPrincipal();
		userDetails.setUser(updateUser);
	}

	/**
	 * Deletes the given user.
	 * Removes all permissions of the user.
	 *
	 * @param user The user to be deleted.
	 */
	public void deleteUser(final RandimiUser user) {
		aclEntryRepository.deleteByAclSid(user.getAclSid());
		for (Study assignedStudy : user.getAssignedStudies()) {
			assignedStudy.getAssignedUsers().remove(user);
			studyRepository.save(assignedStudy);
		}
		user.getAssignedStudies().clear();
		randimiUserRepository.delete(user);
	}

	/**
	 * Enables the given user.
	 * @param user The user to be enabled.
	 */
	public void enableUser(final RandimiUser user) {
		user.setEnabled(true);
		randimiUserRepository.save(user);
	}

	/**
	 * Disables the given user and logs them out.
	 * @param user The user to be disabled.
	 */
	public void disableUser(final RandimiUser user) {
		user.setEnabled(false);
		randimiUserRepository.save(user);
		logoutUser(user);
	}

	/**
	 * Creates a new invitation token.
	 * @return The new invitation token.
	 */
	private String createInvitationToken() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * Creates a new time stamp for the expiration of the invitation for the given user.
	 * @param user User with the invitation.
	 * @return Expiration time of the user's invitation.
	 */
	public Timestamp getInvitationExpirationTimestamp(final RandimiUser user) {
		return new Timestamp(user.getInvitationTimestamp().getTime() + INVITATION_EXPIRATION_THRESHOLD);
	}

	/**
	 * Checks whether the user's invitation is expired.
	 * @param user The user.
	 * @return True if the invitation is expired.
	 */
	public boolean isInvitationExpired(final RandimiUser user) {
		return System.currentTimeMillis() > getInvitationExpirationTimestamp(user).getTime();
	}

	/**
	 * Creates the invitation link for the given user using their invitation token and the address of the given request.
	 * @param request Request used for creating the URL.
	 * @param user User with a valid invitation token.
	 * @return URL for the invitation.
	 */
	public String assembleInvitationLink(HttpServletRequest request, RandimiUser user) {
		return request.getRequestURL().subSequence(0, request.getRequestURL().lastIndexOf("/users")) +
		       "/users/activate?token=" + user.getInvitationToken();
	}

	/**
	 * Creates a timestamp so that earlier timestamps of ForgotPasswordTokens are expired.
	 * @return The timestamp.
	 */
	public Timestamp getForgotPasswordTokenExpirationTimestamp() {
		return new Timestamp(System.currentTimeMillis() - FORGOT_PASSWORD_TOKEN_EXPIRATION_THRESHOLD);
	}

	/**
	 * Creates a new unique ForgotPasswordToken for the given user.
	 * @param user The user.
	 * @return The new token.
	 */
	@Transactional
	public ForgotPasswordToken createForgotPasswordToken(final RandimiUser user) {
		final ForgotPasswordToken token = new ForgotPasswordToken();

		do {
			token.renewToken();
		} while (forgotPasswordTokenRepository.findFirstByToken(token.getToken()) != null);

		token.setRandimiUser(user);
		forgotPasswordTokenRepository.save(token);
		return token;
	}

	/**
	 * Renews the given token by creating a new token and resetting the creation timestamp.
	 * @param token The token to be renewed.
	 */
	@Transactional
	public void renewForgotPasswordToken(final ForgotPasswordToken token) {
		while (forgotPasswordTokenRepository.findFirstByToken(token.getToken()) != null) {
			token.renewToken();
		}
		forgotPasswordTokenRepository.save(token);
	}

	/**
	 * Checks if the given {@link ForgotPasswordToken} is expired.
	 * Returns true if the given token is null.
	 *
	 * @param forgotPasswordToken The token to check.
	 * @return If the token is expired.
	 */
	public boolean isForgotPasswordTokenExpired(@Nullable final ForgotPasswordToken forgotPasswordToken) {
		if (forgotPasswordToken == null) {
			return true;
		}

		return System.currentTimeMillis() - forgotPasswordToken.getTimestamp().getTime() >
		       FORGOT_PASSWORD_TOKEN_EXPIRATION_THRESHOLD;
	}

	/**
	 * Invalidates the session of the given user and logs them out.
	 * @param user The user to be logged out.
	 */
	private void logoutUser(final RandimiUser user) {
		final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

		for (final Object principal : allPrincipals) {
			if (principal instanceof UserDetails userDetails) {
				if (userDetails.getUsername().equals(user.getUsername())) {
					final List<SessionInformation> sessionInformationList = sessionRegistry.getAllSessions(userDetails,
					                                                                                       false);
					for (final SessionInformation sessionInformation : sessionInformationList) {
						sessionInformation.expireNow();
					}
				}
			}
		}
	}
}
