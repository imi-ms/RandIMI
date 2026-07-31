package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.AclClass;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the AclClass class.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Tobias Brix
 */
@Transactional(readOnly = true)
public interface AclClassRepository extends CrudRepository<AclClass, Long> {

	AclClass findFirstByClassNameOrSynonym(String className, String synonym);
	
}
