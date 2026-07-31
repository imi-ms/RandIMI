package de.unimuenster.imi.randimi.dto.settings;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Setter @Getter
public class PseudonymRegexDTO {
	
	private Long id = 0L;

	private Integer orderNumber = 0;

	private List<PseudonymRegexDescriptionDTO> pseudonymRegexDescriptionDTOList = new ArrayList<>();

	private String regex;
}
