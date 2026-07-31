package de.unimuenster.imi.randimi.repository.study;

import de.unimuenster.imi.randimi.model.study.StudyArm;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the StudyArm class.
 * 
 * @author Tobais Brix
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface StudyArmRepository extends CrudRepository<StudyArm, Long> {
}
