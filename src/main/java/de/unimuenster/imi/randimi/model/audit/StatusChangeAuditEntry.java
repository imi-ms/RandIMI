package de.unimuenster.imi.randimi.model.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

/**
 * Data class containing relevant information of a status change for the audit log.
 *
 * @author Daniel Preciado-Marquez
 */
@Getter @Setter
@NoArgsConstructor
public class StatusChangeAuditEntry {

	/**
	 * Status of the study.
	 */
	private StudyStatus status;

	/**
	 * Retention period of the study.
	 */
	@Nullable
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate retentionPeriod;
}
