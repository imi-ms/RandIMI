package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.EntityBase;
import jakarta.persistence.*;
import jakarta.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Database representation of each user. Name "user" is a keyword in Postgres.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Entity
public class RandimiUser extends EntityBase {

	/**
	 * Unique user name used for authentication.
	 */
	@Column(unique = true, nullable = false)
	@Getter
	private String username;

	@JoinColumn(name = "sid", unique = true, nullable = false)
	@OneToOne
	@Cascade(CascadeType.ALL)
	@Getter @Setter
	private AclSid aclSid;

	/**
	 * Password used for authentication.
	 */
	@Column
	@Getter
	private String password;

	/**
	 * Allows to disable users temporarily.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private boolean enabled = true;

	/**
	 * Full user name.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String firstName;

	/**
	 * Full user name.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String lastName;

	/**
	 * E-Mail
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String eMail;


	/**
	 * List of roles associated with this user.
	 */
	@OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@Getter
	private final List<UserRole> userRoles = new ArrayList<>();

	public void addUserRole(UserRole userRole) {
		userRoles.add(userRole);
		if (userRole.getUser() != this) {
			userRole.setUser(this);
		}
	}

	public void removeUserRole(UserRole userRole) {
		userRoles.remove(userRole);
		if (userRole.getUser() != null) {
			userRole.setUser(null);
		}
	}

	public void removeUserRole(final UserRoles userRoles) {
		UserRole target = null;
		for (final UserRole userRole: this.userRoles) {
			if (userRole.getEnumRole() == userRoles) {
				target = userRole;
				break;
			}
		}

		if (target != null) {
			removeUserRole(target);
		}
	}

	public boolean hasUserRole(final UserRoles userRoles) {
		boolean hasRole = false;
		for (final UserRole userRole: this.userRoles) {
			if (userRole.getEnumRole() == userRoles) {
				hasRole = true;
				break;
			}
		}
		return hasRole;
	}

	/**
	 * Invitation token for the user, is null when activated.
	 */
	@Column(name = "invitation_token")
	@Getter @Setter
	private String invitationToken;

	/**
	 * Invitation timestamp for the user, is null when activated.
	 */
	@Column(name = "invitation_timestamp")
	@Getter @Setter
	@Nullable
	private Timestamp invitationTimestamp;

	/**
	 * The user that invited this user.
	 * Mapped by {@link #invitedUsers}.
	 * Is null for the initial administrator or if the user was deleted or for users invited before RandIMI version 2.3.0.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invited_by", referencedColumnName = "username", insertable = false, updatable = false, nullable = true)
	@Getter
	@Nullable
	private RandimiUser invitedByUser;

	/**
	 * The username of the user that invited that user.
	 * Is null for the initial administrator or users invited before RandIMI version 2.3.0.
	 * If the user was deleted, the name is still available.
	 */
	@Column(name = "invited_by")
	@Getter
	@Nullable
	private String invitedByUsername;

	/**
	 * Other users invited by this user.
	 * Mapped by {@link #invitedByUser}
	 * Users invited before RandIMI version 2.3.0 are not listed.
	 */
	@OneToMany(mappedBy = "invitedByUser")
	@Getter
	private final Set<RandimiUser> invitedUsers = new HashSet<>();

	@OneToOne(mappedBy = "randimiUser", orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	@Getter
	private ForgotPasswordToken forgotPasswordToken;

	@ManyToMany(targetEntity = Study.class, mappedBy = "assignedUsers", fetch = FetchType.LAZY)
	@Getter
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE})
	private final Set<Study> assignedStudies = new HashSet<>();

	//-------------------------------------------------------------------------
	//  Gravatar related
	//-------------------------------------------------------------------------

	/**
	 * Use Gravatar?
	 * If the Admin has enabled Gravatar on the Server, each user an individually
	 * choose, Grabator should be applied,
	 */
	@Column(nullable=false)
	@Getter @Setter
	private boolean gravatarEnabled;

	/**
	 * String used in the Gravatar url.
	 * @return MD5 version of the eMail.
	 */
	public String getGravatarHash() {
		String myHash;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(this.eMail.getBytes());
			byte[] digest = md.digest();
			myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			return "00000000000000000000000000000000";
		}
		return myHash;
	}

	//-------------------------------------------------------------------------
	//  Constructor
	//-------------------------------------------------------------------------

	public RandimiUser() {
	}

	public RandimiUser(String username, String password, boolean enabled, String firstName, String lastName, String eMail, List<String> userRoles, boolean gravatar) {
		this.aclSid = new AclSid(username, true);
		this.username = username.toUpperCase();
		this.setPassword(password);
		this.enabled = enabled;
		this.firstName = firstName;
		this.lastName = lastName;
		this.eMail = eMail;
		this.userRoles.clear();
		for (String userRole : userRoles) {
			this.addUserRole(new UserRole(UserRoles.fromString(userRole)));
		}
		this.gravatarEnabled = gravatar;
	}

	public void setForgotPasswordToken(final ForgotPasswordToken newToken) {
		final ForgotPasswordToken oldUser = this.forgotPasswordToken;
		this.forgotPasswordToken = newToken;
		if (oldUser != null && oldUser.getRandimiUser() == this) {
			oldUser.setRandimiUser(null);
		}
		if (newToken != null && newToken.getRandimiUser() != this) {
			newToken.setRandimiUser(this);
		}
	}

	public static RandimiUser newUser(String username) {
		RandimiUser user = new RandimiUser();
		user.setAclSid(new AclSid(username, true));
		user.setUsername(username);
		return user;
	}

	public void setPassword(String password) {
		if (password != null) {
			this.password = new BCryptPasswordEncoder().encode(password);
		} else {
			this.password = null;
		}
	}

	public void setUsername(String username) {
		this.username = username.toUpperCase();
	}

	public Boolean hasRole(UserRoles checkUserRole) {
		for (UserRole userRole : userRoles) {
			if (userRole.getEnumRole().equals(checkUserRole)) {
				return true;
			}
		}
		return false;
	}

	public void addAssignedStudy(final Study study) {
		this.assignedStudies.add(study);
		if (!study.getAssignedUsers().contains(this)) {
			study.addAssignedUser(this);
		}
	}

	public void setInvitedBy(@Nullable final RandimiUser newInvitedBy) {
		final RandimiUser oldInvitedBy = this.invitedByUser;
		this.invitedByUser = newInvitedBy;
		if (oldInvitedBy != null && oldInvitedBy.getInvitedUsers().contains(this)) {
			oldInvitedBy.removeInvitedUser(this);
		}
		if (newInvitedBy != null && !newInvitedBy.getInvitedUsers().contains(this)) {
			newInvitedBy.addInvitedUser(this);
		}

		if (newInvitedBy != null) {
			this.invitedByUsername = newInvitedBy.getUsername();
		}
	}

	public void addInvitedUser(final RandimiUser invitedUser) {
		this.invitedUsers.add(invitedUser);
		if (invitedUser.getInvitedByUser() != this) {
			invitedUser.setInvitedBy(this);
		}
	}

	public void removeInvitedUser(final RandimiUser invitedUser) {
		this.invitedUsers.remove(invitedUser);
		if (invitedUser.getInvitedByUser() == this) {
			invitedUser.setInvitedBy(null);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RandimiUser)) {
			return false;
		}
		RandimiUser other = (RandimiUser) obj;
		return this.getId() == other.getId();
	}

}
