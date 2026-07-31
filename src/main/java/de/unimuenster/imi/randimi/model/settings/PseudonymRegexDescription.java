package de.unimuenster.imi.randimi.model.settings;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.EntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Daniel Preciado-Marquez
 */
@Entity
public class PseudonymRegexDescription extends EntityBase {

    /**
     * Language of the message.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private SupportedLanguage currentLanguage;

    /**
     * Meaningful description of the regex.
     * Maximum of 512 kb text.
     */
    @Column(nullable = false, columnDefinition = "TEXT", length = 512 * 1024)
    @Getter
    @Setter
    private String description;

    /**
     * Meaningful name of the regex.
     */
    @Column(nullable = false)
    @Getter
    @Setter
    private String name;

    /**
     * Associated pseudonym regex of this description.
     */
    @ManyToOne(optional = false)
    @Getter
    private PseudonymRegex pseudonymRegex;

    public void setPseudonymRegex(final PseudonymRegex pseudonymRegex) {
       final PseudonymRegex oldPseudonymRegex = this.pseudonymRegex;
       this.pseudonymRegex = pseudonymRegex;
       if (oldPseudonymRegex != null && oldPseudonymRegex.getPseudonymRegexDescriptionList().contains(this))
           oldPseudonymRegex.removePseudonymRegexDescription(this);
       if (pseudonymRegex != null && !pseudonymRegex.getPseudonymRegexDescriptionList().contains(this))
           pseudonymRegex.addPseudonymRegexDescription(this);
    }

}
