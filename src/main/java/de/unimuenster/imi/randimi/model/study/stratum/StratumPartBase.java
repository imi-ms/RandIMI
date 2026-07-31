package de.unimuenster.imi.randimi.model.study.stratum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unimuenster.imi.randimi.model.EntityBase;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * Database representation of each stratum part. (Baseclass)
 * Subclasses can be intervals (float) of enumerations (String)
 *
 * @author Tobias Brix
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
		@JsonSubTypes.Type(value = StratumPartEnumeration.class, name = "StratumPartEnumeration"),
		@JsonSubTypes.Type(value = StratumPartInterval.class, name = "StratumPartInterval"),
		@JsonSubTypes.Type(value = StratumPartSite.class, name = "StratumPartSite")}
)
@Getter
public abstract class StratumPartBase extends EntityBase {

	/**
	 * Associated stratum of this interval.
	 */
	@JsonIgnore
	@ToString.Exclude
	@ManyToOne
	private Stratum stratum;

	/**
	 * Optional order number of the interval. It is used to display the intervals in a fix order.
	 */
	@Column
	@Setter
	private int orderNumber;

	@Formula("(select s.order_number from stratum s where s.id = stratum_id)")
	private int stratumOrderNumber;

	@JsonIgnore
	@ToString.Exclude
	@ManyToMany(mappedBy = "stratumParts")
	private final List<SubjectList> subjectLists = new ArrayList<>();

	public void setStratum(Stratum stratum) {
		Stratum oldStratum = this.stratum;
		this.stratum = stratum;
		if (oldStratum != null && oldStratum.getStratumParts().contains(this)) {
			this.stratum.removeStratumPart(this);
		}
		if (stratum != null && !stratum.getStratumParts().contains(this)) {
			stratum.addStratumPart(this);
		}
	}

	public void addSubjectList(final SubjectList subjectList) {
		if (!subjectLists.contains(subjectList)) {
			subjectLists.add(subjectList);
		}
		if (!subjectList.getStratumParts().contains(this)) {
			subjectList.addStratumPart(this);
		}
	}

	public void removeSubjectList(final SubjectList subjectList) {
		subjectLists.remove(subjectList);
		if (subjectList.getStratumParts().contains(this)) {
			subjectList.removeStratumPart(this);
		}
	}

	/**
	 * Checks, if an element is contained in the stratum part. 
	 * If the data type is not compatible, false is returned.
	 *
	 * @param value Element to check
	 * @return If the element is contained or not.
	 */
	public abstract boolean isValueContainedInStratumPart(Object value);

	@Transient
	public abstract String getPartKey();

	@Transient
	public abstract String getName();
}
