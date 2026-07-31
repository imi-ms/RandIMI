package de.unimuenster.imi.randimi.model.user;

import java.util.Objects;

import de.unimuenster.imi.randimi.model.EntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Entity
@Table(name = "acl_sid")
public class AclSid extends EntityBase {

	@Column(name = "sid")
	@Getter
	@Setter
	private String sid;
	
	@Column(name = "principal")
	@Getter
	@Setter
    private boolean principal;
	
	public AclSid() {
		
	}
	
	public AclSid(String sid, boolean principal) {
		setSid(sid);
		setPrincipal(principal);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.getId());
		hash = 59 * hash + Objects.hashCode(this.sid);
		hash = 59 * hash + (this.principal ? 1 : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AclSid other = (AclSid) obj;
		if (!Objects.equals(this.getId(), other.getId())) {
			return false;
		}
		return true;
	}
}
