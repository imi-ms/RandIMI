package de.unimuenster.imi.randimi.model;

import de.unimuenster.imi.randimi.model.enumeration.AuditClass;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

/**
 * A model to represent an entry of the audit trail. Includes all necessary
 * information.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Entity
public class AuditEntry extends EntityBase {

	@Column(nullable = false)
	@Getter
	@Setter
	private Timestamp timestamp;

	@Getter
	@Setter
	private String username;

	@Column(nullable = false)
	@Getter
	@Setter
	private long studyId;

	@Column(nullable = false)
	@Getter
	@Setter
	private long targetId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private AuditClass auditClass;

	@Column(nullable = true)
	@Getter
	@Setter
	private String reason;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private AuditType auditType;

	@Column(columnDefinition="TEXT")
	@Getter
	@Setter
	private String content;

	@Column(columnDefinition="TEXT")
	@Getter
	@Setter
	private String oldContent;
}
