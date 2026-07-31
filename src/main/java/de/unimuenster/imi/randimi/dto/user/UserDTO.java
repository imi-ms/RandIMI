package de.unimuenster.imi.randimi.dto.user;

import de.unimuenster.imi.randimi.model.enumeration.UserEditStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class UserDTO {

	@Getter
	@Setter
	private long id;

	@Getter
	@Setter
	private String username;

	@Getter
	@Setter
	private String password;

	@Getter
	@Setter
	private String repeatPassword;

	@Getter
	@Setter
	private boolean enabled;

	@Getter
	@Setter
	private String firstName;

	@Getter
	@Setter
	private String lastName;

	@Getter
	@Setter
	private String eMail;

	@Getter
	@Setter
	private List<String> userRoles;

	@Getter
	@Setter
	private UserEditStatus status;

	@Getter
	@Setter
	private String invitationToken;

	/**
	 * Username of the user that invited this user.
	 */
	@Getter @Setter
	@Nullable
	private String invitedBy;

	@Getter
	@Setter
	private boolean skipEMailValidation;

}
