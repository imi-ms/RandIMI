package de.unimuenster.imi.randimi.repository.subject;

import de.unimuenster.imi.randimi.model.subject.SubjectList;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the RandomizationList class.
 * 
 * @author Tobais Brix
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SubjectListRepository extends CrudRepository<SubjectList, Long> {
}
