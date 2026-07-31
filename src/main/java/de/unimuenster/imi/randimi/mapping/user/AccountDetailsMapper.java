package de.unimuenster.imi.randimi.mapping.user;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccountDetailsMapper {

	private final RandimiUserRepository randimiUserRepository;

	@Autowired
	public AccountDetailsMapper(final RandimiUserRepository randimiUserRepository) {
		this.randimiUserRepository = randimiUserRepository;
	}

	public AccountDetailsDTO toAccountDetailsDTO(final RandimiUser user) {
		final AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();

		accountDetailsDTO.setId(user.getId());
		accountDetailsDTO.setUsername(user.getUsername());
		accountDetailsDTO.setFirstName(user.getFirstName());
		accountDetailsDTO.setLastName(user.getLastName());
		accountDetailsDTO.setMailAddress(user.getEMail());
		accountDetailsDTO.setUpdatePassword(false);
		accountDetailsDTO.setGravatarEnabled(user.isGravatarEnabled());

		return accountDetailsDTO;
	}

	public RandimiUser toRandimiUser(final AccountDetailsDTO accountDetailsDTO) {
		final RandimiUser randimiUser = randimiUserRepository.findById(accountDetailsDTO.getId()).get();
		return toRandimiUser(accountDetailsDTO, randimiUser);
	}

	public RandimiUser toRandimiUser(final AccountDetailsDTO accountDetailsDTO, final RandimiUser randimiUser) {
		randimiUser.setUsername(accountDetailsDTO.getUsername());
		randimiUser.setFirstName(accountDetailsDTO.getFirstName());
		randimiUser.setLastName(accountDetailsDTO.getLastName());
		randimiUser.setEMail(accountDetailsDTO.getMailAddress());

		if (accountDetailsDTO.getUpdatePassword())
			randimiUser.setPassword(accountDetailsDTO.getNewPassword());

		randimiUser.setGravatarEnabled(accountDetailsDTO.getGravatarEnabled());

		return randimiUser;
	}

}
