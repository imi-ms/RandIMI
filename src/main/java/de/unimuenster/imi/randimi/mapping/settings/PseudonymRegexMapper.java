package de.unimuenster.imi.randimi.mapping.settings;

import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDTO;
import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDescriptionDTO;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.settings.PseudonymRegex;
import de.unimuenster.imi.randimi.model.settings.PseudonymRegexDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class PseudonymRegexMapper {

    private final PseudonymRegexDescriptionMapper pseudonymRegexDescriptionMapper;

    @Autowired
    public PseudonymRegexMapper(final PseudonymRegexDescriptionMapper pseudonymRegexDescriptionMapper) {
        this.pseudonymRegexDescriptionMapper = pseudonymRegexDescriptionMapper;
    }

    /**
     * Converts this {@link PseudonymRegex} object to an {@link PseudonymRegexDTO} object.
     *
     * @return An {@link PseudonymRegexDTO} object based on this {@link PseudonymRegex} object.
     */
    public PseudonymRegexDTO toPseudonymRegexDTO(final PseudonymRegex pseudonymRegex) {
        PseudonymRegexDTO pseudonymRegexDTO = new PseudonymRegexDTO();

        pseudonymRegexDTO.setId(pseudonymRegex.getId());
        pseudonymRegexDTO.setOrderNumber(pseudonymRegex.getOrderNumber());

        // Sort descriptions by the supported language
        for (final PseudonymRegexDescription pseudonymRegexDescription : pseudonymRegex.getPseudonymRegexDescriptionList())
            pseudonymRegexDTO.getPseudonymRegexDescriptionDTOList()
                    .add(pseudonymRegexDescriptionMapper.toPseudonymRegexDescriptionDTO(pseudonymRegexDescription));
        pseudonymRegexDTO.getPseudonymRegexDescriptionDTOList()
                         .sort(Comparator.comparing(a -> SupportedLanguage.INDICES.get(a.getCurrentLanguage())));

        pseudonymRegexDTO.setRegex(pseudonymRegex.getRegex());

        return pseudonymRegexDTO;
    }

    public PseudonymRegex toPseudonymRegex(PseudonymRegexDTO dto) {
        PseudonymRegex pseudonymRegex = new PseudonymRegex();

        pseudonymRegex.setId(dto.getId());
        pseudonymRegex.setOrderNumber(dto.getOrderNumber());

        for (final PseudonymRegexDescriptionDTO pseudonymRegexDescriptionDTO : dto.getPseudonymRegexDescriptionDTOList())
            pseudonymRegex.addPseudonymRegexDescription(
                    pseudonymRegexDescriptionMapper.toPseudonymRegexDescription(pseudonymRegexDescriptionDTO));

        pseudonymRegex.setRegex(dto.getRegex());

        return pseudonymRegex;
    }
}
