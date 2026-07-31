package de.unimuenster.imi.randimi.model.study;

import de.unimuenster.imi.randimi.controller.helper.RandimiHelper;
import de.unimuenster.imi.randimi.model.NamedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

/**
 * Class representing a research site.
 * If studies are stratified by site, the stratum name "location" will be used for sites.
 */
@Entity
@Getter
public class Site extends NamedEntity {

    /**
     * Order number of the site.
     * It is used to display the sites in a fixed order
     * and for the order number of the site stratum parts.
     */
    @Column(nullable = false)
    @Setter
    private int orderNumber;

    @ManyToOne
    private Study study;

    /**
     * Max number of patients in this site.
     */
    @Column(nullable = false)
    @Setter
    private int capacity;

    /**
     * Seed for the randomization.
     */
    @Column(nullable = true)
    @Setter
    private long seed;

    /**
     * Number of times the random has been called.
     */
    @Column(nullable = false)
    @Setter
    private int randomCalls;

    /**
     * Optional GUI-name.
     */
    @Column(nullable = false)
    @Setter
    private String pseudonymRegex = ".*";

    public void setStudy(final Study study) {
        final Study oldStudy = this.study;
        this.study = study;
        if (oldStudy != null && oldStudy.getSites().contains(this)) {
            oldStudy.removeSite(this);
        }
        if (study != null && !study.getSites().contains(this)) {
            study.addSite(this);
        }
    }

    public int nextRandomInt(int bound) {
		Random random = RandimiHelper.getRandom(seed, randomCalls);
		randomCalls++;
		return random.nextInt(bound);
    }

    public float nextRandomFloat() {
        Random random = RandimiHelper.getRandom(seed, randomCalls);
        randomCalls++;
        return random.nextFloat();
    }
}
