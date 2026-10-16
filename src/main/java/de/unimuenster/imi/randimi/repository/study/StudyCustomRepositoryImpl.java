package de.unimuenster.imi.randimi.repository.study;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.study.Study;
import org.springframework.stereotype.Component;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;

import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class StudyCustomRepositoryImpl implements StudyCustomRepository {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StudyCustomRepositoryImpl.class);

	private final AclClassRepository aclClassRepository;
	private final AclObjectIdentityRepository aclObjectIdentityRepository;
	private final AclEntryRepository aclEntryRepository;
	private final SubjectRepository subjectRepository;

	public StudyCustomRepositoryImpl(
			AclClassRepository aclClassRepository,
			AclObjectIdentityRepository aclObjectIdentityRepository,
			AclEntryRepository aclEntryRepository,
			SubjectRepository subjectRepository
	) {
		this.aclClassRepository = aclClassRepository;
		this.aclObjectIdentityRepository = aclObjectIdentityRepository;
		this.aclEntryRepository = aclEntryRepository;
		this.subjectRepository = subjectRepository;
	}

	@Override
	public Optional<Subject> findRegistered(final Study study, final long siteId, String pseudonym) {
		return switch (study.getPseudonymHandling()) {
			case UNIQUE_IN_STUDY ->
					subjectRepository.findFirstBySubjectListStudyIdAndPseudonym(study.getId(), pseudonym);
			case UNIQUE_IN_LOCATION ->
					subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(
							pseudonym, siteId, study.getId());
		};
	}

	@Override
	public void grantRight(Study study, AclSid aclSid, PermissionType right) {
		try {
			// Get the corresponding ACL class of the given element
			AclClass elementClass = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(),
			                                                                         StudyDTO.class.getName());
			// Get the database ID for the element
			Long elementId = study.getId();
			// Create a new ACLObjectIdentity for the element and save it
			AclObjectIdentity elementObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
					elementClass, elementId);
			AclEntry elementUserAccess = aclEntryRepository.findFirstByObjectAndAclSidAndPermissionType(
					study, aclSid, right);
			// If the user does not have the given right for the given object. grant it
			if (elementUserAccess == null) {
				elementUserAccess = new AclEntry(aclSid, elementObjectIdentity, 1, right, true, false, false);
				aclEntryRepository.persist(elementUserAccess);
			}
		} catch (IllegalArgumentException | SecurityException ex) {
			LOGGER.error("Error during granting the right " + right + " for entity of type "
			             + Study.class.getName() + " and user with ID " + aclSid.getId()
			             + ". Check if there is a consistent database.");
		}
	}

	@Override
	public void revokeRight(Study study, AclSid aclSid, PermissionType right) {
		AclEntry elementUserAccess = aclEntryRepository.findFirstByObjectAndAclSidAndPermissionType(
				study, aclSid, right);
		if (elementUserAccess != null) {
			aclEntryRepository.delete(elementUserAccess);
		}
	}

}
