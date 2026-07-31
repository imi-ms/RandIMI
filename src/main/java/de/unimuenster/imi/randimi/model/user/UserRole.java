package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.EntityBase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Database representation of each user role.
 * 
 * @author Tobias Brix
 */
@Entity
public class UserRole extends EntityBase {
	
	public UserRole () {}
	
	public UserRole (UserRoles userRole) {
		enumRole = userRole;
	} 
	
	@ManyToOne
	@Getter
	private RandimiUser user;
	public void setUser(RandimiUser user) {
		RandimiUser oldUser = this.user;
		this.user = user;
		if (oldUser != null && oldUser.getUserRoles().contains(this)) {
			oldUser.removeUserRole(this);
		}
		if (user != null && !user.getUserRoles().contains(this)) {
			user.addUserRole(this);
		}
	}
	
	@Column
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private UserRoles enumRole;
}
