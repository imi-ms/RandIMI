package de.unimuenster.imi.randimi.model.audit;

import de.unimuenster.imi.randimi.model.study.StudyArm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class SubjectAuditEntry {

	@Getter @Setter
	private String pseudonym;

	@Getter @Setter
	private String site;

	@Getter @Setter
	private String[] enumeratedStrata;

	@Getter @Setter
	private StudyArm studyArm;

	@Getter @Setter
	private String status;
}
