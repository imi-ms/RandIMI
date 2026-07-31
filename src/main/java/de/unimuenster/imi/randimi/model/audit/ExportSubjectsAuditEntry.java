package de.unimuenster.imi.randimi.model.audit;

import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ExportSubjectsAuditEntry {
	private final Long studyId;
	private final Set<String> sites;
	private final Set<SubjectStatus> status;
	private final Map<String, Set<String>> strata;
}
