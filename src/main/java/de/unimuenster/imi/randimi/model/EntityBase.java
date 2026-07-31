package de.unimuenster.imi.randimi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Used to handle recurring methods and attributes.
 * 
 * @author Tobias Brix
 */
@MappedSuperclass
@Getter @Setter
public abstract class EntityBase implements Serializable {
	
	/**
	 * ID used in the internal database.
	 */
	@JsonIgnore
	@Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "hibernate_sequence")
	@SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
	private long id;
}
