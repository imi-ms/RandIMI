package de.unimuenster.imi.randimi.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Superclass for entities with a name and an API ID.
 *
 * @author Daniel Preciado-Marquez
 */
@MappedSuperclass
@Getter @Setter
public abstract class NamedEntity extends EntityBase {
	/**
	 * GUI-name.
	 */
	@Column(nullable = false)
	private String guiName;

	/**
	 * ID used for identifying the study via AIP communication.
	 */
	@Column(nullable = false)
	private String apiId;

	/**
	 * If the API ID should be synchronized with the name.
	 */
	@Column(nullable = false)
	private boolean synchronizeApiId = true;
}
