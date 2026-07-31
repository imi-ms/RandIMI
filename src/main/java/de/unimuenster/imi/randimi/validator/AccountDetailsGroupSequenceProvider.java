package de.unimuenster.imi.randimi.validator;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.ArrayList;
import java.util.List;

public class AccountDetailsGroupSequenceProvider implements DefaultGroupSequenceProvider<AccountDetailsDTO> {
	@Override
	public List<Class<?>> getValidationGroups(AccountDetailsDTO object) {
		List<Class<?>> groups = new ArrayList<>();

		if (object != null) {
			if (object.getUpdatePassword()) {
				groups.add(PasswordChangeValidation.class);
			}
		}

		groups.add(AccountDetailsDTO.class);
		return groups;
	}
}
