package de.unimuenster.imi.randimi.dto.study;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.model.enumeration.PseudonymHandling;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@Setter @Getter
public class StudyDTO extends NamesDTO {

	@JsonIgnore
	private Long id;

	private String description;

	private List<SiteDTO> sites = new ArrayList<>();

	private RandomizationAlgorithm randomizationAlgorithm;

	private Boolean preGenerateSubjectList;

	private Integer minBlocksize;

	private Integer maxBlocksize;

	@Nullable
	private MinimizationParameterDTO minimizationParameter;

	private PseudonymHandling pseudonymHandling;

	@JsonIgnore
	private Timestamp activationDate;

	@Nullable
	private Timestamp retentionPeriod;

	private StudyStatus status = StudyStatus.INEXISTENT;

	private Integer capacity;

	private List<StudyArmDTO> studyArms = new ArrayList<>();

	private List<StratumDTO> enumeratedStratums = new ArrayList<>();

	@JsonIgnore
	private List<StratumDTO> intervalStratums = new ArrayList<>();

	/**
	 * Site stratum of the study.
	 * Null if the study does not have a site stratum.
	 * Null after received from the front end.
	 */
	@JsonIgnore
	private StratumDTO siteStratum;

	private boolean stratifyBySite;

	public StudyDTO(long id) {
		this.id = id;
	}

	/**
	 * Returns a list with all API IDs of the sites in this study.
	 *
	 * @return A list with all API IDs of the sites in this study.
	 */
	@JsonIgnore
	public List<String> getSiteApiIds() {
		return sites.stream().map(SiteDTO::getApiId).toList();
	}

	/**
	 * Convenience function to test if the status of this study is {@link StudyStatus#INEXISTENT}.
	 * @return If the status of this study is {@link StudyStatus#INEXISTENT}.
	 */
	@JsonIgnore
	public boolean isInexistent() {
		return this.status == StudyStatus.INEXISTENT;
	}

	@JsonIgnore
	public boolean isActivated() {
		return this.activationDate != null;
	}

	/**
	 * Convenience function to test if the status of this study is {@link StudyStatus#CREATED}.
	 * @return If the status of this study is {@link StudyStatus#CREATED}.
	 */
	@JsonIgnore
	public boolean isCreated() {
		return this.status == StudyStatus.CREATED;
	}

	/**
	 * Convenience function to test if the study is in test mode.
	 * @return If the study is in test mode.
	 */
	@JsonIgnore
	public boolean isInTestMode() {
		return this.status == StudyStatus.TESTING;
	}

	/**
	 * Convenience function to test if the study is active.
	 * @return If the study is active.
	 */
	@JsonIgnore
	public boolean isActive() {
		return this.status == StudyStatus.ACTIVE;
	}

	/**
	 * Convenience function to test if the study is locked.
	 * @return If the study is locked.
	 */
	@JsonIgnore
	public boolean isLocked() {
		return this.status == StudyStatus.LOCKED;
	}

	/**
	 * Convenience function to test if the study is archived.
	 * @return If the study is archived.
	 */
	@JsonIgnore
	public boolean isArchived() {
		return this.status == StudyStatus.ARCHIVED;
	}

	/**
	 * Convenience function to test if the study is deleted.
	 * @return If the study is deleted.
	 */
	@JsonIgnore
	public boolean isDeleted() {
		return this.status == StudyStatus.DELETED;
	}

	/**
	 * Convenience function to test if the study is in a state where statistics are available.
	 *
	 * @return If the study is in a state where statistics are available.
	 */
	@JsonIgnore
	public boolean isStatisticsAvailable() {
		return isActive() || isLocked() || isArchived();
	}

}
