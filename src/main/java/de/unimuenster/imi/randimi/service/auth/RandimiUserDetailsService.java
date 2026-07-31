package de.unimuenster.imi.randimi.service.auth;

import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;

import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loading user details.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Service
public class RandimiUserDetailsService implements UserDetailsService {
	public static final String USER_DETAILS_SERVICE_NAME = "randimiUserDetailsService";

	private final RandimiUserRepository userRepository;

	@Autowired
	public RandimiUserDetailsService(final RandimiUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final RandimiUser userEntity = userRepository.findFirstByUsernameIgnoreCase(username);
		// Check if user is empty and throw exception
		if (userEntity == null || userEntity.getPassword() == null || userEntity.getPassword().isEmpty())
			throw new UsernameNotFoundException("User not found");
		return new MyUserDetails(userEntity);
	}
}
