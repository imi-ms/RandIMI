package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.dto.user.ForgotPasswordTokenDTO;
import de.unimuenster.imi.randimi.model.EntityBase;
import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Database representation of the forgot password token.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Entity
public class ForgotPasswordToken extends EntityBase {

	@OneToOne(optional = false)
	@JoinColumn(name = "randimi_user_id", referencedColumnName = "id", nullable = false)
	@Getter
	private RandimiUser randimiUser;
	
	@Column(nullable = false)
	@Getter @Setter
	private String token;
	
	@Column(nullable = false)
	@Getter @Setter
	private Timestamp timestamp;
	
	public ForgotPasswordToken(RandimiUser user) {
		this.randimiUser = user;
		this.renewToken();
		user.setForgotPasswordToken(this);
	}
	
	public ForgotPasswordToken(){}

	public void setRandimiUser(final RandimiUser newUser) {
		final RandimiUser oldUser = this.randimiUser;
		this.randimiUser = newUser;
		if (oldUser != null && oldUser.getForgotPasswordToken() == this) {
			oldUser.setForgotPasswordToken(null);
		}
		if (newUser != null && newUser.getForgotPasswordToken() != this) {
			newUser.setForgotPasswordToken(this);
		}
	}
	
	/**
	 * Renews the password token. Is Always a random UUID without the "-".
	 */
	public void renewToken() {
		this.token = UUID.randomUUID().toString().replace("-", "");
		this.timestamp = new Timestamp(System.currentTimeMillis());
	}

	public ForgotPasswordTokenDTO toForgotPasswordTokenDTO() {
		ForgotPasswordTokenDTO forgotPasswordTokenDTO = new ForgotPasswordTokenDTO();
		forgotPasswordTokenDTO.setId(this.getId());
		forgotPasswordTokenDTO.setUserId(this.getRandimiUser().getId());
		forgotPasswordTokenDTO.setToken(token);
		return forgotPasswordTokenDTO;
	}
}
