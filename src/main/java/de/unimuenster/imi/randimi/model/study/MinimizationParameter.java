package de.unimuenster.imi.randimi.model.study;


import de.unimuenster.imi.randimi.model.EntityBase;
import de.unimuenster.imi.randimi.model.enumeration.ImbalanceFunction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Class containing the parameter for the minimization algorithm.
 *
 * @author Daniel Preciado-Marquez
 */
@Entity
@Getter @Setter
public class MinimizationParameter extends EntityBase {

	/**
	 * If the ratio of study arms should be forced.
	 */
	@Column(nullable = false)
	private boolean forceRatio = true;

	/**
	 * The probability to draw the study arm with the highest imbalance.
	 */
	@Column(nullable = false)
	private float imbalanceBias = 0.7f;

	/**
	 * Function for calculating the imbalance.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ImbalanceFunction imbalanceFunction = ImbalanceFunction.VARIANCE;

	/**
	 * The corresponding study.
	 * Don't set this directly, instead us {@link Study#setMinimizationParameter(MinimizationParameter)}.
	 */
	@OneToOne(optional = false)
	@JoinColumn(name = "study_id", referencedColumnName = "id", nullable = false)
	private Study study;
}
