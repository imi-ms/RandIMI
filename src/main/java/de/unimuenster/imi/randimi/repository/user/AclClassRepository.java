package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.AclClass;
import org.jspecify.annotations.Nullable;
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

	@Nullable
	AclClass findFirstByClassNameOrSynonym(@Nullable String className, @Nullable String synonym);
	
}
