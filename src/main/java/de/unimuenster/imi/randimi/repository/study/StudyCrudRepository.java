package de.unimuenster.imi.randimi.repository.study;

import de.unimuenster.imi.randimi.model.study.Study;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface StudyCrudRepository extends CrudRepository<Study, Long> {

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

}
