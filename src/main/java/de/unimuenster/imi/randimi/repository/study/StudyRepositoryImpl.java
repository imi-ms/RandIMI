package de.unimuenster.imi.randimi.repository.study;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class StudyRepositoryImpl {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StudyRepository.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	@Lazy
	StudyRepository studyRepository;

	@Autowired
	private AclClassRepository aclClassRepository;

	@Autowired
	private AclObjectIdentityRepository aclObjectIdentityRepository;

	@Autowired
	private AclEntryRepository aclEntryRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	public boolean isPseudonymRegistered(Study study, long siteId, String pseudonym) {
		switch (study.getPseudonymHandling()) {
		case UNIQUE_IN_STUDY:
			return subjectRepository.countBySubjectListStudyIdAndPseudonym(study.getId(), pseudonym) != 0L;
		case UNIQUE_IN_LOCATION:
			return subjectRepository.countBySubjectListStudyIdAndPseudonymAndSiteId(study.getId(), pseudonym, siteId) != 0L;
		default:
			return true;
		}
	}

	/**
	 * See {@link StudyRepository#findRegistered(Study, long, String)} for documentation.
	 */
	public Optional<Subject> findRegistered(final Study study, final long siteId, String pseudonym) {
		return switch (study.getPseudonymHandling()) {
			case UNIQUE_IN_STUDY ->
					subjectRepository.findFirstBySubjectListStudyIdAndPseudonym(study.getId(), pseudonym);
			case UNIQUE_IN_LOCATION ->
					subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(pseudonym, siteId, study.getId());
		};
	}

	public void grantRight(Study study, AclSid aclSid, PermissionType right) {
		try {
			// Get the corresponding ACL class of the given element
			AclClass elementClass = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(),
			                                                                         StudyDTO.class.getName());
			// Get the database Id for the element
			Long elementId = study.getId();
			// Create a new ACLObjectIdentity for the element and save it
			AclObjectIdentity elementObjectIdentity = aclObjectIdentityRepository
					.findFirstByObjectIdClassAndObjectIdIdentity(elementClass, elementId);
			AclEntry elementUserAccess = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study, aclSid,
			                                                                                            right);
			// If the user does not have the given right for the given object. grant it
			if (elementUserAccess == null) {
				elementUserAccess = new AclEntry(aclSid, elementObjectIdentity, 1, right, true, false, false);
				aclEntryRepository.persist(elementUserAccess);
			}
		} catch (IllegalArgumentException | SecurityException ex) {
			LOGGER.error("Error during granting the right " + right.toString() + " for entity of type "
			             + Study.class.getName() + " and user with ID " + aclSid.getId()
			             + ". Check if there is a consistent database.");
		}
	}

	public void revokeRight(Study study, AclSid aclSid, PermissionType right) {
		AclEntry elementUserAccess = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study, aclSid,
				right);
		if (elementUserAccess != null) {
			aclEntryRepository.delete(elementUserAccess);
		}
	}

}
