package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.model.EntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * The database table model for <i>acl_class</i>. The Access Control List (ACL)
 * framework allows to administrate access rights for every different object
 * that is 'registered' as an {@link AclClass}. This model holds the information
 * about the fully qualified name for each of the classes administrated with
 * ACLs in Randimi. An instance of this class represents a record about a single
 * class in the Randimi model.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Entity
@Table(name = "acl_class")
public class AclClass extends EntityBase {

	@Column(name = "class", nullable = false)
	@Getter
	@Setter
	private String className;
	
	@Column(name = "synonym")
	@Getter
	@Setter
	private String synonym;

	public AclClass() {

	}

	public AclClass(String className) {
		setClassName(className);
	}
}
