package de.unimuenster.imi.randimi.dto.user;

import de.unimuenster.imi.randimi.validator.*;
import de.unimuenster.imi.randimi.validator.user.UniqueUsernameConstraint;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.group.GroupSequenceProvider;

@GroupSequenceProvider(AccountDetailsGroupSequenceProvider.class)
@PasswordMatches(message = "{validator.user.passwordMismatch}", groups = PasswordChangeValidation.class)
@PasswordCorrect(passwordFieldName = "oldPassword", message = "{validator.user.oldPasswordMismatch}",
                 groups = PasswordChangeValidation.class)
@UniqueUsernameConstraint(groups = {Default.class, PasswordChangeValidation.class})
public class AccountDetailsDTO {

	@Getter @Setter
	@NotNull(groups = {Default.class, PasswordChangeValidation.class})
	private long id;

	@Getter @Setter
	@NotBlank(message = "{validator.general.mustNotBeEmpty}",
	          groups = {Default.class, PasswordChangeValidation.class})
	@Size(max = 254, message = "{validator.general.mustNotBeLongerThanChars}",
	      groups = {Default.class, PasswordChangeValidation.class})
	@Pattern(regexp = "[^@]*", message = "{validator.user.username.mustNotContainAt}",
	         groups = {Default.class, PasswordChangeValidation.class})
	private String username;

	@Getter @Setter
	@NotBlank(message = "{validator.general.mustNotBeEmpty}",
	          groups = {Default.class, PasswordChangeValidation.class})
	@Size(max = 254, message = "{validator.general.mustNotBeLongerThanChars}",
	      groups = {Default.class, PasswordChangeValidation.class})
	private String firstName;

	@Getter @Setter
	@NotBlank(message = "{validator.general.mustNotBeEmpty}",
	          groups = {Default.class, PasswordChangeValidation.class})
	@Size(max = 254, message = "{validator.general.mustNotBeLongerThanChars}",
	      groups = {Default.class, PasswordChangeValidation.class})
	private String lastName;

	@Getter @Setter
	@NotBlank(message = "{validator.general.mustNotBeEmpty}",
	          groups = {Default.class, PasswordChangeValidation.class})
	@Size(max = 254, message = "{validator.general.mustNotBeLongerThanChars}",
	      groups = {Default.class})
	@Email(message = "{validator.user.mailInvalid}", groups = {Default.class, PasswordChangeValidation.class})
	private String mailAddress;

	@Getter @Setter
	private Boolean updatePassword;

	@Getter @Setter
	@NotBlank(message = "{validator.user.password.mustNotBeEmpty}", groups = PasswordChangeValidation.class)
	@PasswordStrong(groups = PasswordChangeValidation.class)
	private String newPassword;

	@Getter @Setter
	@NotBlank(message = "{validator.user.password.mustNotBeEmpty}", groups = PasswordChangeValidation.class)
	private String oldPassword;

	@Getter @Setter
	@NotBlank(message = "{validator.user.password.mustNotBeEmpty}", groups = PasswordChangeValidation.class)
	private String repeatPassword;
	
	@Getter @Setter
	private Boolean gravatarEnabled;
}
