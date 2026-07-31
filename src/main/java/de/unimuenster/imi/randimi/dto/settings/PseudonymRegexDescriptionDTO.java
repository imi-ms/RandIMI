package de.unimuenster.imi.randimi.dto.settings;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Daniel Preciado-Marquez
 */
@Setter @Getter
public class PseudonymRegexDescriptionDTO {

    private Long id = 0L;

    private SupportedLanguage currentLanguage;

    private String description;

    private String name;

}
