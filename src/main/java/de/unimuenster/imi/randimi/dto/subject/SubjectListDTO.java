package de.unimuenster.imi.randimi.dto.subject;

import java.util.List;

import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Getter @Setter
public class SubjectListDTO {

	private long Id;

	private long studyId;

	private List<StratumPartBaseDTO> stratumParts;
	
	private List<SubjectEntryDTO> subjectEntries;
}
