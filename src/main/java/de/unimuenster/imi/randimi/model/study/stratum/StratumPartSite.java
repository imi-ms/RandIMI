package de.unimuenster.imi.randimi.model.study.stratum;

import de.unimuenster.imi.randimi.model.study.Site;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Subclass of stratum part base.
 * It represents the site of the subject.
 *
 * @author Daniel Preciad-Marquez
 */
@Entity
@ToString
@Getter @Setter
public class StratumPartSite extends StratumPartBase {

    /**
     * The corresponding site.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private Site site;

    /**
     * Checks, if the given value is contained in the stratum part.
     * Compares the given value with the name of the site.
     *
     * @param value Value to check.
     * @return If the value is part of the stratum part.
     */
    @Override
    public boolean isValueContainedInStratumPart(Object value) {
        if (value instanceof String apiId) {
	        return site.getApiId().equals(apiId);
        }
        return false;
    }

    @Override
    @Transient
    public String getPartKey() {
        return getSite().getApiId();
    }

    @Override
    public String getName() {
        return getSite().getGuiName();
    }
}
