package de.unimuenster.imi.randimi.repository.study.stratum;

import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the StratumPartBase class.
 * 
 * @author Tobais Brix
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface StratumPartBaseRepository extends CrudRepository<StratumPartBase, Long> {
}
