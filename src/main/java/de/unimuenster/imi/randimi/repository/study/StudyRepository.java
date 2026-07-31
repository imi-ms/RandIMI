package de.unimuenster.imi.randimi.repository.study;

import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the Study class.
 *
 * @author Tobais Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface StudyRepository extends CrudRepository<Study, Long> {

	@Override
	@PostFilter("@customPermissionEvaluator.hasPermissionStudy(authentication, filterObject, 'READ_STUDY')")
	List<Study> findAll();

	List<Study> findByGuiName(String guiName);

	Optional<Study> findByApiId(String apiId);

	List<Study> findByRetentionPeriodIs(Timestamp retentionPeriod);

	boolean existsByGuiNameOrApiId(String guiName, String apiId);

	@Query(value = "select apiId from Study")
	List<String> getApiIds();

	@Query(value = "select guiName from Study")
	List<String> getNames();

	/**
	 * Checks if a conflict with existing pseudonyms exists.
	 * If the study enforces unique pseudonyms across sites, all subjects are checked.
	 * Otherwise, only subjects from the same site are checked.
	 * Implemented by {@link StudyRepositoryImpl#findRegistered(Study, long, String)}
	 *
	 * @param study The study.
	 * @param siteId The ID of the site of the subject.
	 * @param pseudonym The pseudonym of the subject.
	 * @return The subject that causes the conflict or an empty optional.
	 */
	Optional<Subject> findRegistered(Study study, long siteId, String pseudonym);

	public void grantRight(Study study, AclSid aclSid, PermissionType right);

	public void revokeRight(Study study, AclSid aclSid, PermissionType right);
}
