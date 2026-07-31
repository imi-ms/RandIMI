package de.unimuenster.imi.randimi.dto;

import de.unimuenster.imi.randimi.model.enumeration.AuditClass;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class AuditEntryDTO {

	@Getter
	@Setter
	private Timestamp timestamp;
	
	@Getter
	@Setter
	private String username;
	
	@Getter
	@Setter
	private String studyName;

	@Getter
	@Setter
	private AuditClass auditClass;
	
	@Getter
	@Setter
	private String reason;
	
	@Getter
	@Setter
	private AuditType auditType;
	
	@Getter
	@Setter
	private String content;

	@Getter
	@Setter
	private String oldContent;
}
