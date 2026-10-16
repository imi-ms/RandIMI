package de.unimuenster.imi.randimi.repository.study;

import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the Study class.
 *
 * @author Tobais Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface StudyRepository extends StudyCrudRepository, StudyCustomRepository {
}
