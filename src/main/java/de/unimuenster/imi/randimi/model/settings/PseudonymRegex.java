package de.unimuenster.imi.randimi.model.settings;

import de.unimuenster.imi.randimi.model.EntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.HashSet;
import java.util.Set;

/**
 * CLass that holds the pseudonym regex. Consisting of a name, description and 
 * the regex.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Entity
public class PseudonymRegex extends EntityBase {
	
	/**
	 * Associated settings of this pseudonym regex.
	 */
	@ManyToOne(optional = false)
	@Getter
	private Settings settings;
	
	public void setSettings(Settings settings) {
		Settings oldSettings = this.settings;
		this.settings = settings;
		if (oldSettings != null && oldSettings.getPseudonymRegexList().contains(this)) {
			oldSettings.removePseudonymRegex(this);
		}
		if (settings != null && !settings.getPseudonymRegexList().contains(this)) {
			settings.addPseudonymRegex(this);
		}
	}

	/**
	 * Order number to ensure a fix order of the entries.
	 */
	@Column(nullable = false)
	@Getter
	@Setter
	private int orderNumber;

	@OneToMany(mappedBy = "pseudonymRegex", orphanRemoval = true, fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@Getter
	private final Set<PseudonymRegexDescription> pseudonymRegexDescriptionList = new HashSet<>();

	/**
	 * Regular Expression that the pseudonym must match.
	 */
	@Column(nullable = false)
	@Getter
	@Setter
	private String regex;

	public void addPseudonymRegexDescription(final PseudonymRegexDescription pseudonymRegexDescription) {
		pseudonymRegexDescriptionList.add(pseudonymRegexDescription);
		if (pseudonymRegexDescription.getPseudonymRegex() != this)
			pseudonymRegexDescription.setPseudonymRegex(this);
	}

	public void removePseudonymRegexDescription(final PseudonymRegexDescription pseudonymRegexDescription) {
		pseudonymRegexDescriptionList.remove(pseudonymRegexDescription);
		if (pseudonymRegexDescription.getPseudonymRegex() != null)
			pseudonymRegexDescription.setPseudonymRegex(null);
	}
}
