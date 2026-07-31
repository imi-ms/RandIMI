package de.unimuenster.imi.randimi.model.subject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.EntityBase;
import de.unimuenster.imi.randimi.model.study.Study;

import java.util.*;

import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * Database representation of each subject list.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Entity
@Getter
public class SubjectList extends EntityBase {

	/**
	 * Array containing the remaining assignments for each study arm in the current block.
	 * The values are aligned by the order number of the study arms.
	 * Null if the randomization is not 'BLOCKED' or the study is not activated.
	 * On activation, the values are initialized to 0.
	 */
	@Column(columnDefinition = "int[]", nullable = true)
	@Setter
	private Integer[] remainingAssignments;

	/**
	 * List of stratum parts all subjects have.
	 * Contains one stratum part for each stratum of the study, ordered by the order number of the corresponding stratum.
	 * Is empty if the study has no strata.
	 */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "subject_lists_stratum_parts",
			joinColumns = @JoinColumn(name = "subject_list_id"),
			inverseJoinColumns = @JoinColumn(name = "stratum_part_base_id")
	)
	@OrderBy("stratumOrderNumber")
	private final List<StratumPartBase> stratumParts = new ArrayList<>();

	/**
	 * List of each randomization entry.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "subjectList", orphanRemoval = true)
	@OrderBy("orderNumber")
	@Cascade(CascadeType.ALL)
	private final List<Subject> subjects = new ArrayList<>();

	/**
	 * Associated study of this list.
	 */
	@JsonIgnore
	@ManyToOne(optional = false)
	private Study study;

	public void setStudy(Study study) {
		Study oldStudy = this.study;
		this.study = study;
		if (oldStudy != null && oldStudy.getSubjectLists().contains(this)) {
			oldStudy.removeSubjectList(this);
		}
		if (study != null && !study.getSubjectLists().contains(this)) {
			study.addSubjectList(this);
		}
	}

	public void addStratumPart(final StratumPartBase stratumPartBase) {
		if (!stratumParts.contains(stratumPartBase)) {
			stratumParts.add(stratumPartBase);
		}
		if (!stratumPartBase.getSubjectLists().contains(this)) {
			stratumPartBase.addSubjectList(this);
		}
	}

	public void addAllStratumParts(final Collection<StratumPartBase> stratumParts) {
		for (final StratumPartBase stratumPart : stratumParts) {
			addStratumPart(stratumPart);
		}
	}

	public void removeStratumPart(final StratumPartBase stratumPartBase) {
		stratumParts.remove(stratumPartBase);
		if (stratumPartBase.getSubjectLists().contains(this)) {
			stratumPartBase.removeSubjectList(this);
		}
	}

	public void addSubject(Subject subject) {
		subjects.add(subject);
		if (subject.getSubjectList() != this) {
			subject.setSubjectList(this);
		}
	}

	public void removeSubject(Subject subject) {
		subjects.remove(subject);
		if (subject.getSubjectList() != null) {
			subject.setSubjectList(null);
		}
	}

	public void addAllRandomizationEntries(List<Subject> subjects) {
		for (Subject entry : subjects) {
			addSubject(entry);
		}
	}

	public void removeAllRandomizationEntries() {
		for (int i = getSubjects().size() - 1; i >= 0; i--) {
			removeSubject(getSubjects().get(i));
		}
	}

	/**
	 * Returns a {@link Subject} with the given Id, if its associated to the list.
	 * Otherwise returns null.
	 *
	 * @param entryId Id of the searched entry.
	 *
	 * @return A {@link Subject} with the given Id, if its associated to the list.
	 *         Otherwise null.
	 */
	public Subject getSubjectById(long entryId) {
		for (Subject entry : getSubjects()) {
			if (entry.getId() == entryId) {
				return entry;
			}
		}
		return null;
	}

	public int size() {
		return subjects.size();
	}

}
