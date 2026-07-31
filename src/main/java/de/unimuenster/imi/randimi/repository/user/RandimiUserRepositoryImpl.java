package de.unimuenster.imi.randimi.repository.user;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Preciado-Marquez <daniel.preciado.marquez@uni-muenster.de>
 */
@Component
public class RandimiUserRepositoryImpl {

	@Autowired
	@Lazy
	RandimiUserRepository randimiUserRepository;

	@Autowired
	AclObjectIdentityRepository aclObjectIdentityRepository;

	@Autowired
	AclClassRepository aclClassRepository;

	@Autowired
	AclEntryRepository aclEntryRepository;

	public boolean doesUsernameAlreadyExist(String username, long id) {
		RandimiUser randimiUser = randimiUserRepository.findFirstByUsernameIgnoreCase(username);
		return (randimiUser == null || randimiUser.getId() == id) ? false : true;
	}

	public boolean isUserActive(long id) {
		Optional<RandimiUser> randimiUser = randimiUserRepository.findById(id);
		return randimiUser.isPresent() && randimiUser.get().isEnabled();
	}

	public List<RandimiUser> getObsoleteUsers() {
		return randimiUserRepository.findByInvitationTimestampNotNullAndInvitationTimestampLessThan(
				Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
	}

	public List<RandimiUser> getNotifiedUsersOfStudy(Study study) {
		AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), Study.class.getName()),
				study.getId());
		List<AclSid> correspondingSids = aclEntryRepository.getAllAclSidsForAclObjectIdentityAndRight(aclObjectIdentity,
		                                                                                              PermissionType.GET_NOTIFICATION);

		if (correspondingSids.isEmpty())
			return new ArrayList<>();

		return randimiUserRepository.findByAclSidIn(correspondingSids);
	}
}
