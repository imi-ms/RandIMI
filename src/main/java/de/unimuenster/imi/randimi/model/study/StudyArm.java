package de.unimuenster.imi.randimi.model.study;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.NamedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Database representation of each study arm.
 * 
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Entity
@ToString
@Getter @Setter
//@Table(	uniqueConstraints = @UniqueConstraint(columnNames={"study_id", "orderNumber"}))
public class StudyArm extends NamedEntity {
	
	/**
	 * Associated study of this study arm.
	 */
	@JsonIgnore
	@ToString.Exclude
	@ManyToOne(optional = false)
	private Study study;
	
	public void setStudy(Study study) {
		Study oldStudy = this.study;
		this.study = study;
		if (oldStudy != null && oldStudy.getStudyArms().contains(this)) {
			oldStudy.removeStudyArm(this);
		}
		if (study != null && !study.getStudyArms().contains(this)) {
			study.addStudyArm(this);
		}
	}
	
	/**
	 * Order number of the study arm. It is used to display the study arms in a fix order.
	 */
	@JsonIgnore
	@Column(nullable = false)
	private int orderNumber;

	/**
	 * Custom ratio, used to allow custom ratios of subject in study arms.
	 */
	@Column(nullable = false)
	private int ratio;
}
