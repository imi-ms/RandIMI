package de.unimuenster.imi.randimi.repository;

import de.unimuenster.imi.randimi.model.AuditEntry;

import java.util.List;

import de.unimuenster.imi.randimi.model.enumeration.AuditClass;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object for the AuditEntry model.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface AuditEntryRepository extends CrudRepository<AuditEntry, Long> {

	List<AuditEntry> findByStudyId(long studyId);

	void deleteByStudyId(long studyId);

	void deleteByStudyIdAndAuditClass(long studyId, AuditClass auditClass);
}
