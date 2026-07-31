package de.unimuenster.imi.randimi.dto.settings;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */
@Setter @Getter
public class FooterMessageSettingsDTO {
	
	private Long id;
	
	private SupportedLanguage currentLanguage;
	
	private String imprintContent;
	
	private String dataPrivacyContent;

	private String supportContent;

}
