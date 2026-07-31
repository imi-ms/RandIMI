package de.unimuenster.imi.randimi.repository.study;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SiteRepository extends CrudRepository<Site, Long> {

	List<Site> findByStudy(Study study);

	List<Site> findByStudyId(long studyId);

	Optional<Site> findByStudyIdAndId(Long studyId, Long id);

	Optional<Site> findByStudyIdAndApiId(Long studyId, String apiId);

	Optional<Site> findByStudyApiIdAndApiId(String studyApiId, String apiId);

	Pair<List<SiteDTO>, List<Site>> getNewAndDeletedSites(StudyDTO studyDTO);
}
