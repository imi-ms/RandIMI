package de.unimuenster.imi.randimi.model.study.stratum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.NamedEntity;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.Study;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Database representation of each stratum.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Entity
@AttributeOverride(name = "guiName", column = @Column(name = "name"))
@ToString
@Getter
public class Stratum extends NamedEntity {

	/**
	 * Type of this stratum.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Setter
	private StratumType stratumType;

	/**
	 * Associated study of this stratum.
	 */
	@JsonIgnore
	@ToString.Exclude
	@ManyToOne(optional = false)
	private Study study;

	/**
	 * Order number of the stratum. It is used to display the strata in a fix order.
	 * The site stratum always has the highest order number.
	 */
	@Column(nullable = false)
	@Setter
	private int orderNumber;

	/**
	 * List of parts associated to this stratum.
	 * Can be of type interval (float) or of type enumeration (String)
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy="stratum", orphanRemoval = true)
	@Cascade(CascadeType.ALL)
    @OrderBy("orderNumber")
	@Setter
	private List<StratumPartBase> stratumParts = new ArrayList<>();

	/**
	 * Wrapper to bo consistent with the DB column name.
	 *
	 * @return The name of the stratum.
	 */
	public String getName() {
		return getGuiName();
	}

	/**
	 * Wrapper to bo consistent with the DB column name.
	 *
	 * @param name The new name of the stratum
	 */
	public void setName(final String name) {
		setGuiName(name);
	}

	public void setStudy(Study study) {
		Study oldStudy = this.study;
		this.study = study;
		if (oldStudy != null && oldStudy.getStratums().contains(this)) {
			oldStudy.removeStratum(this);
		}
		if (study != null && !study.getStratums().contains(this)) {
			study.addStratum(this);
		}
	}

	public void addStratumPart(StratumPartBase stratumPart) {
		stratumParts.add(stratumPart);
		if (stratumPart.getStratum() != this) {
			stratumPart.setStratum(this);
		}
	}

	public void addAllStratumParts(final Collection<StratumPartBase> stratumParts) {
		for (final StratumPartBase stratumPart : stratumParts) {
			addStratumPart(stratumPart);
		}
	}

	public void removeStratumPart(StratumPartBase stratumPart) {
		stratumParts.remove(stratumPart);
		if (stratumPart.getStratum() != null) {
			stratumPart.setStratum(null);
		}
	}

	/**
	 * Searches the StratumPartEntity for the given value.
	 * @param value The value of the stratum part.
	 * @return The corresponding StratumPartBase object.
	 */
	@Nullable
	public StratumPartBase getStratumPartByValue(final Object value) {
		for (final StratumPartBase stratumPart : stratumParts) {
			if (stratumPart.isValueContainedInStratumPart(value)) {
				return stratumPart;
			}
		}
		return null;
	}
}
