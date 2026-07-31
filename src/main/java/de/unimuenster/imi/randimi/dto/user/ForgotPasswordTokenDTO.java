package de.unimuenster.imi.randimi.dto.user;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class ForgotPasswordTokenDTO {
	
	@Getter
	@Setter
	private long id;

	@Getter
	@Setter
	private long userId;
	
	@Getter
	@Setter
	private String token;
	
	@Getter
	@Setter
	private String password;
	
	@Getter
	@Setter
	private String repeatPassword;
}
