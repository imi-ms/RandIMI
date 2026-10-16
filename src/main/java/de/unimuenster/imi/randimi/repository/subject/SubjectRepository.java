package de.unimuenster.imi.randimi.repository.subject;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.subject.Subject;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Data access object used for the RandomizationEntry class.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SubjectRepository extends SubjectCrudRepository, SubjectCustomRepository {
}
