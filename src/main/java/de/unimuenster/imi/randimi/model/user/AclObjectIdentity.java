package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.model.EntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * The database table model for <i>acl_object_identity</i>. The Access Control
 * List (ACL) framework maintains the permissions for instances of 'registered'
 * classes of the Randimi model. The AclObjectIdentity contains the information
 * for an object of type <i>object_id_class</i>. The Identity of a class is
 * referenced via primary keys, which are retrieved from its origin database.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Entity
@Table(name = "acl_object_identity")
public class AclObjectIdentity extends EntityBase {

	@JoinColumn(name = "object_id_class", referencedColumnName = "id")
    @ManyToOne
	@Getter
	@Setter
    private AclClass objectIdClass;
	
	@Column(name = "object_id_identity")
	@Getter
	@Setter
    private Long objectIdIdentity;
	
	@JoinColumn(name = "parent_object", referencedColumnName = "id")
    @ManyToOne
	@Getter
	@Setter
    private AclObjectIdentity parentObject;
	
	@JoinColumn(name = "owner_sid", referencedColumnName = "id")
    @ManyToOne
	@Getter
	@Setter
    private AclSid aclSid;
	
	@Column(name = "entries_inheriting")
	@Getter
	@Setter
    private Boolean entriesInheriting;
	
	public AclObjectIdentity() {
		
	}
	
	public AclObjectIdentity(Long objectIdIdentity, Boolean entriesInheriting, AclClass objectIdClass, AclSid aclSid, AclObjectIdentity parentObject) {
        setObjectIdIdentity(objectIdIdentity);
        setEntriesInheriting(entriesInheriting);
        setObjectIdClass(objectIdClass);
        setAclSid(aclSid);
    }
}
