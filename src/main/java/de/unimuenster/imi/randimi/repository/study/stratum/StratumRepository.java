package de.unimuenster.imi.randimi.repository.study.stratum;

import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the Stratum class.
 * 
 * @author Tobais Brix
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface StratumRepository extends CrudRepository<Stratum, Long> {
}
