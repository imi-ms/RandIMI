package de.unimuenster.imi.randimi.mapping.settings;

import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDescriptionDTO;
import de.unimuenster.imi.randimi.model.settings.PseudonymRegexDescription;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class PseudonymRegexDescriptionMapper {

    public PseudonymRegexDescriptionDTO toPseudonymRegexDescriptionDTO(final PseudonymRegexDescription pseudonymRegexDescription) {
        final PseudonymRegexDescriptionDTO pseudonymRegexDescriptionDTO = new PseudonymRegexDescriptionDTO();

        pseudonymRegexDescriptionDTO.setId(pseudonymRegexDescription.getId());
        pseudonymRegexDescriptionDTO.setCurrentLanguage(pseudonymRegexDescription.getCurrentLanguage());
        pseudonymRegexDescriptionDTO.setDescription(pseudonymRegexDescription.getDescription());
        pseudonymRegexDescriptionDTO.setName(pseudonymRegexDescription.getName());

        return pseudonymRegexDescriptionDTO;
    }

    public PseudonymRegexDescription toPseudonymRegexDescription(final PseudonymRegexDescriptionDTO pseudonymRegexDescriptionDTO) {
        final PseudonymRegexDescription pseudonymRegexDescription = new PseudonymRegexDescription();

        pseudonymRegexDescription.setId(pseudonymRegexDescriptionDTO.getId());
        pseudonymRegexDescription.setCurrentLanguage(pseudonymRegexDescriptionDTO.getCurrentLanguage());
        pseudonymRegexDescription.setDescription(pseudonymRegexDescriptionDTO.getDescription());
        pseudonymRegexDescription.setName(pseudonymRegexDescriptionDTO.getName());

        return pseudonymRegexDescription;
    }

}
