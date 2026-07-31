package de.unimuenster.imi.randimi.model.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class MyUserDetails implements UserDetails {
	
	@Getter
	@Setter
	private RandimiUser user;
	
	public MyUserDetails(final RandimiUser user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
        for (UserRole userRole : user.getUserRoles()) {
            authorities.add(new SimpleGrantedAuthority(userRole.getEnumRole().getTextValue()));
        }
        return Collections.unmodifiableCollection(authorities);
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}
	
	public Long getId() {
		return user.getId();
	}
	
	public String getFullName() {
		return user.getLastName() + ", " + user.getFirstName();
	}
	
	public String getGravatarHash() {
		return user.getGravatarHash();
	}
	
	public boolean isGravatarEnabled() {
		return user.isGravatarEnabled();
	}

	@Override
	public boolean isAccountNonExpired() {
		return user.isEnabled();
	}

	@Override
	public boolean isAccountNonLocked() {
		return user.isEnabled();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return user.isEnabled();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

}
