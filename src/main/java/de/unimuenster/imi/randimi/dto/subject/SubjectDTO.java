package de.unimuenster.imi.randimi.dto.subject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@ToString
public class SubjectDTO {

	@Getter
	@Setter
	private String pseudonym;

	@Getter
	@Setter
	private String location;

	@JsonIgnore
	@Getter
	@Setter
	private long studyId;

	@JsonIgnore
	@Getter
	@Setter
	private String studyApiId;

	@JsonIgnore
	@Getter
	@Setter
	private long siteId;

	@JsonIgnore
	@Getter
	@Setter
	private String siteApiId;

	@Getter
	@Setter
	private String[] enumeratedStratums;

	@JsonIgnore
	@Getter
	@Setter
	private Float[] intervalStratums;
}
