package de.unimuenster.imi.randimi.model.study;

import de.unimuenster.imi.randimi.model.NamedEntity;
import de.unimuenster.imi.randimi.model.enumeration.PseudonymHandling;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;
import java.util.*;

/**
 * Database representation of each study.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Entity
@ToString
@Getter @Setter
public class Study extends NamedEntity {

	/**
	 * Optional description of the study.
	 */
	// 512 kilobytes text
	@Column(nullable = true, length = 512 * 1024)
	private String description;

	/**
	 * Kind of used randomization algorithm.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private RandomizationAlgorithm randomizationAlgorithm;

	@Column(nullable = false)
	private Boolean preGenerateSubjectList = false;

	/**
	 * Minimal size of a block in the blocked algorithm.
	 */
	@Column(nullable = true)
	private Integer minBlocksize;

	/**
	 * Maximal size of a block in the blocked algorithm.
	 */
	@Column(nullable = true)
	private Integer maxBlocksize;

	/**
	 * Parameter for the minimization algorithm.
	 * Only available if the selected randomization algorithm is {@link RandomizationAlgorithm#MINIMIZATION},
	 * otherwise is null.
	 */
	@OneToOne(mappedBy = "study", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	@Nullable
	private MinimizationParameter minimizationParameter;

	/**
	 * Kind of used pseudonymhandling.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PseudonymHandling pseudonymHandling = PseudonymHandling.UNIQUE_IN_LOCATION;

	/**
	 * Date, when the study went active, e.g., the randomization lists have been generated.
	 * Is no null if {@link #status} is {@link StudyStatus#ACTIVE}.
	 */
	@Column(nullable = true)
	private Timestamp activationDate;

	/**
	 * Date when the study should be deleted.
	 * Always null if the study is not archived.
	 * Can be null in archived studies.
	 */
	@Column(nullable = true)
	@Nullable
	private Timestamp retentionPeriod;

	/**
	 * The status of the study.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private StudyStatus status = StudyStatus.CREATED;

	/**
	 * Capacity of subjects in this study.
	 */
	@Column(nullable = false)
	private Integer capacity;

	/**
	 * List of study arms associated with this study. A study has at least one,
	 * possibly many sites which participate in the study. A study can also be
	 * stratified by sites.
	 */
	@OneToMany(mappedBy = "study", orphanRemoval = true, fetch = FetchType.LAZY)
	@Cascade(CascadeType.ALL)
	@OrderBy("orderNumber")
	private final List<StudyArm> studyArms = new ArrayList<>();

	@OneToMany(mappedBy = "study", orphanRemoval = true, fetch = FetchType.LAZY)
	@Cascade(CascadeType.ALL)
	@OrderBy("orderNumber")
	private final List<Site> sites = new ArrayList<>();

	/**
	 * List of randomization lists. One list for each stratum combination of the
	 * study.
	 */
	@OneToMany(mappedBy = "study", orphanRemoval = true, fetch = FetchType.LAZY)
	@Cascade(CascadeType.ALL)
	@ToString.Exclude
	private final List<SubjectList> subjectLists = new ArrayList<>();

	/**
	 * List of strata associated with this study.
	 */
	@OneToMany(mappedBy = "study", orphanRemoval = true, fetch = FetchType.LAZY)
	@Cascade(CascadeType.ALL)
	@OrderBy("orderNumber")
	private final List<Stratum> stratums = new ArrayList<>();

	@Column
	private boolean stratifiedBySite;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "randimi_user_study",
			joinColumns = @JoinColumn(name = "study_id"),
			inverseJoinColumns = @JoinColumn(name = "randimi_user_id"))
	@Cascade(CascadeType.ALL)
	private final Set<RandimiUser> assignedUsers = new HashSet<>();

	public void setMinimizationParameter(final MinimizationParameter minimizationParameter) {
		if (this.minimizationParameter != null) {
			this.minimizationParameter.setStudy(null);
		}

		this.minimizationParameter = minimizationParameter;
		if (this.minimizationParameter != null) {
			this.minimizationParameter.setStudy(this);
		}
	}

	public void addStudyArm(StudyArm studyArm) {
		if (!studyArms.contains(studyArm)) {
			studyArms.add(studyArm);
		}
		if (studyArm.getStudy() != this) {
			studyArm.setStudy(this);
		}
	}

	public void removeStudyArm(StudyArm studyArm) {
		studyArms.remove(studyArm);
		if (studyArm.getStudy() != null) {
			studyArm.setStudy(null);
		}
	}

	public void addSite(Site site) {
		if (!sites.contains(site)) {
			sites.add(site);
		}
		if (site.getStudy() != this) {
			site.setStudy(this);
		}
	}

	/**
	 * Add all sites of the given collection to this study.
	 * Sets also the study ot the given sites.
	 * @param sites Sits to add.
	 */
	public void addAllSites(final Collection<Site> sites) {
		for (final Site site : sites) {
			addSite(site);
		}
	}

	public void removeSite(Site site) {
		sites.remove(site);
		if (site.getStudy() == this) {
			site.setStudy(null);
		}
	}

	@Nullable
	public Site getSiteById(Long id) {
		for (Site site : sites) {
			if (site.getId() == id) {
				return site;
			}
		}
		return null;
	}

	@Nullable
	public Site getSiteByApiId(final String apiId) {
		for (final Site site : sites) {
			if (Objects.equals(site.getApiId(), apiId)) {
				return site;
			}
		}
		return null;
	}

	@Nullable
	public Site getSiteByGuiName(final String guiName) {
		for (Site site : sites) {
			if (site.getGuiName().equals(guiName)) {
				return site;
			}
		}
		return null;
	}

	public void addAllSubjectLists(List<SubjectList> subjectLists) {
		for (SubjectList randomizationList : subjectLists) {
			this.addSubjectList(randomizationList);
		}
	}

	public void addSubjectList(SubjectList subjectList) {
		if (!subjectLists.contains(subjectList)) {
			subjectLists.add(subjectList);
		}
		if (subjectList.getStudy() != this) {
			subjectList.setStudy(this);
		}
	}

	public void removeSubjectList(SubjectList subjectList) {
		subjectLists.remove(subjectList);
		if (subjectList.getStudy() != null) {
			subjectList.setStudy(null);
		}
	}

	public void removeAllSubjectLists(final Collection<SubjectList> subjectLists) {
		for (final SubjectList subjectList : subjectLists) {
			removeSubjectList(subjectList);
		}
	}

	/**
	 * Returns a {@link Subject} with the given Id, if its associated to the site.
	 * Otherwise returns null.
	 *
	 * @param subjectId Id of the searched entry.
	 *
	 * @return A {@link Subject} with the given Id, if its associated to the site.
	 *         Otherwise null.
	 */
	public Subject getSubjectById(long subjectId) {
		for (SubjectList subjectList : subjectLists) {
			Subject subject = subjectList.getSubjectById(subjectId);
			if (subject != null && subject.getId() == subjectId) {
				return subject;
			}
		}
		return null;
	}


	@Nullable
	public Stratum getStratumById(final long stratumId) {
		return stratums.stream()
		               .filter(stratum -> stratum.getId() == stratumId)
		               .findFirst()
		               .orElse(null);
	}

	public Optional<Stratum> getStratumByApiId(final String stratumApiId) {
		return stratums.stream()
		               .filter(stratum -> Objects.equals(stratum.getApiId(), stratumApiId))
		               .findFirst();
	}

	public void addStratum(Stratum stratum) {
		if (!stratums.contains(stratum)) {
			stratums.add(stratum);
		}
		if (stratum.getStudy() != this) {
			stratum.setStudy(this);
		}
	}

	public void addAllStrata(final Collection<Stratum> strata) {
		for (final Stratum stratum : strata) {
			addStratum(stratum);
		}
	}

	public void removeStratum(Stratum stratum) {
		stratums.remove(stratum);
		if (stratum.getStudy() != null) {
			stratum.setStudy(null);
		}
	}

	public void addAssignedUser(final RandimiUser user) {
		assignedUsers.add(user);
		if (!user.getAssignedStudies().contains(this)) {
			user.addAssignedStudy(this);
		}
	}

	/**
	 * Convenience function to test if the study is in test mode.
	 * @return If the study is in test mode.
	 */
	public boolean isInTestMode() {
		return this.status == StudyStatus.TESTING;
	}

	/**
	 * Convenience function to test if the study is active.
	 * @return If the study is active.
	 */
	public boolean isActive() {
		return this.status == StudyStatus.ACTIVE;
	}

	/**
	 * Convenience function to test if the study is archived.
	 * @return If the study is archived.
	 */
	public boolean isArchived() {
		return this.status == StudyStatus.ARCHIVED;
	}

	/**
	 * Convenience function to test if the study is deleted.
	 * @return If the study is deleted.
	 */
	public boolean isDeleted() {
		return this.status == StudyStatus.DELETED;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Study other)) {
			return false;
		}
		return this.getId() == other.getId();
	}

	/**
	 * Validates that minimization parameter is present if the minimization algorithm is selected.
	 */
	@PrePersist @PreUpdate
	private void validateMinimization() {
		if (randomizationAlgorithm == RandomizationAlgorithm.MINIMIZATION && minimizationParameter == null) {
			throw new IllegalArgumentException("Minimization parameter must be set for minimization algorithm.");
		} else if (randomizationAlgorithm != RandomizationAlgorithm.MINIMIZATION && minimizationParameter != null) {
			throw new IllegalArgumentException("Minimization parameter must not be set for other algorithms.");
		}
	}

}
