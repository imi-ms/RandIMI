package de.unimuenster.imi.randimi.dto.study.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.model.enumeration.PermissionBundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Setter @Getter
@NoArgsConstructor
public class StudyUserDTO {

	@JsonIgnore
	private Long userId;

	private String username;

	@JsonIgnore
	private String userFirstName;

	@JsonIgnore
	private String userLastName;

	private Set<PermissionBundle> studyPermissionBundles;

	private Set<PermissionBundle> allSitePermissionBundles = new HashSet<>();

	private Map<String, Set<PermissionBundle>> sitePermissionBundles = new HashMap<>();

	public StudyUserDTO(final RandimiUser user, final Set<PermissionBundle> studyPermissionBundles, final Set<PermissionBundle> allSitePermissionBundles) {
		this.userId = user.getId();
		this.username = user.getUsername();
		this.userFirstName = user.getFirstName();
		this.userLastName = user.getLastName();
		this.studyPermissionBundles = studyPermissionBundles;
		this.allSitePermissionBundles = allSitePermissionBundles;
	}
}
