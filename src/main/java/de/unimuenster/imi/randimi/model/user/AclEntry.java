package de.unimuenster.imi.randimi.model.user;

import de.unimuenster.imi.randimi.model.EntityBase;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * The database table model for <i>acl_entry</i>. The Access Control List (ACL)
 * framework secures a given class and the model <i>AclEntry</i> contains the
 * actual permission for a given class' object and given user.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Entity
@Table(name = "acl_entry")
public class AclEntry extends EntityBase {

	@Column(name = "ace_order")
	@Getter
	@Setter
    private Integer aceOrder;
	
	@Column(name = "mask")
	@Enumerated(EnumType.ORDINAL)
	@Getter
	@Setter
	private PermissionType permissionType;
	
	@Column(name = "granting")
	@Getter
	@Setter
    private Boolean granting;
	
	@Column(name = "audit_success")
	@Getter
	@Setter
    private Boolean auditSuccess;
	
	@Column(name = "audit_failure")
	@Getter
	@Setter
    private Boolean auditFailure;
	
	@JoinColumn(name = "sid", referencedColumnName = "id")
    @ManyToOne
	@Getter
	@Setter
    private AclSid aclSid;
	
	@JoinColumn(name = "acl_object_identity", referencedColumnName = "id")
    @ManyToOne
	@Getter
	@Setter
    private AclObjectIdentity aclObjectIdentity;
	
	public AclEntry() {
		
	}
	
	public AclEntry(AclSid aclSid, AclObjectIdentity aclObjectIdentity, int aceOrder, PermissionType permissionType, boolean granting, boolean auditSuccess, boolean auditFailure) {
        setAclSid(aclSid);
        setAclObjectIdentity(aclObjectIdentity);
		setAceOrder(aceOrder);
        setPermissionType(permissionType);
        setGranting(granting);
        setAuditSuccess(auditSuccess);
        setAuditFailure(auditFailure);
        
    }

}
