package de.unimuenster.imi.randimi.model.subject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.EntityBase;
import de.unimuenster.imi.randimi.model.study.Site;

import java.sql.Timestamp;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Database representation of each randomization entry.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Entity
@ToString
@Getter @Setter
public class Subject extends EntityBase {

	public Subject() {
	}

	public Subject(int orderNumber, StudyArm studyArm) {
		this.orderNumber = orderNumber;
		this.studyArm = studyArm;
	}

	/**
	 * Associated randomization list of this interval.
	 */
	@JsonIgnore
	@ToString.Exclude
	@ManyToOne(optional = false)
	private SubjectList subjectList;

	public void setSubjectList(SubjectList subjectList) {
		SubjectList oldSubjectList = this.subjectList;
		this.subjectList = subjectList;
		if (oldSubjectList != null && oldSubjectList.getSubjects().contains(this)) {
			oldSubjectList.removeSubject(this);
		}
		if (subjectList != null && !subjectList.getSubjects().contains(this)) {
			subjectList.addSubject(this);
		}
	}

	/**
	 * Order number to ensure a fix order of the entries.
	 */
	@Column(nullable = false)
	private int orderNumber;

	/**
	 * The study arm, this entry represents.
	 */
	@ManyToOne(optional = false)
	private StudyArm studyArm;

	/**
	 * Timestamp of the randomization.
	 */
	private Timestamp randomizationTimestamp = null;

	/**
	 * Timestamp of the deletion.
	 */
	@Nullable
	private Timestamp deletionTimestamp = null;

	/**
	 * Timestamp of the release.
	 */
	@Nullable
	private Timestamp releaseTimestamp = null;

	/**
	 * Indicator that shows whether the subject is existent, deleted or released.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private SubjectStatus status = SubjectStatus.ACTIVE;

	/**
	 * Contains null if not assigned yet, or the pseudonym of the patient if
	 * successfully assigned.
	 */
	@Column(nullable = true)
	private String pseudonym = null;

	@ManyToOne(optional = true)
	private Site site;

	public void setAssignedTo(Site site, String pseudo) {
		setPseudonym(pseudo);
		setSite(site);
		setRandomizationTimestamp(new Timestamp(System.currentTimeMillis()));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 43 * hash + Objects.hashCode(this.subjectList);
		hash = 43 * hash + this.orderNumber;
		hash = 43 * hash + Objects.hashCode(this.studyArm);
		hash = 43 * hash + Objects.hashCode(this.pseudonym);
		hash = 43 * hash + Objects.hashCode(this.site);
		return hash;
	}

	@Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Subject)) {
            return false;
        }
        Subject other = (Subject) obj;
        return this.getId() == other.getId();
    }
}
