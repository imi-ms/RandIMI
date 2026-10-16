package de.unimuenster.imi.randimi.repository.study;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.user.AclSid;

import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
public interface StudyCustomRepository {

	/**
	 * Checks if a conflict with existing pseudonyms exists.
	 * If the study enforces unique pseudonyms across sites, all subjects are checked.
	 * Otherwise, only subjects from the same site are checked.
	 *
	 * @param study The study.
	 * @param siteId The ID of the site of the subject.
	 * @param pseudonym The pseudonym of the subject.
	 * @return The subject that causes the conflict or an empty optional.
	 */
	Optional<Subject> findRegistered(Study study, long siteId, String pseudonym);

	void grantRight(Study study, AclSid aclSid, PermissionType right);

	void revokeRight(Study study, AclSid aclSid, PermissionType right);

}
