package de.unimuenster.imi.randimi.mapping.study.user;

import java.util.*;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import de.unimuenster.imi.randimi.dto.study.user.StudyUserDTO;
import de.unimuenster.imi.randimi.model.enumeration.PermissionBundle;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;

/**
 * Class for mapping assigned users of a study to a StudyUserDTO.
 *
 * @author Daniel Preciado-Marquez
 */
@Component
public class StudyUserMapper {

	private final AclEntryRepository aclEntryRepository;
	private final RandimiUserRepository randimiUserRepository;

	@Autowired
	public StudyUserMapper(final AclEntryRepository aclEntryRepository,
	                       final RandimiUserRepository randimiUserRepository) {
		this.aclEntryRepository = aclEntryRepository;
		this.randimiUserRepository = randimiUserRepository;
	}

	/**
	 * Maps an entire study with all assigned users to a List of StudyUserDTO.
	 * F
	 *
	 * @param study The study to be mapped.
	 * @return List of StudyUserDTO for all assigned users.
	 */
	public List<StudyUserDTO> toStudyUserDTOs(final Study study) {
		final List<StudyUserDTO> studyUserDTOs = new ArrayList<>();

		if (study != null && study.getId() != 0) {
			final Map<AclSid, Set<PermissionType>> userPermissionsMap = aclEntryRepository.getUserRightsByObject(study);
			final Map<Long, Set<PermissionBundle>> userStudyPermissions = getPermissionBundlesForUsersAndStudy(study.getAssignedUsers(), userPermissionsMap);
			final Map<Long, Set<PermissionBundle>> userAllSitePermissions = getPermissionBundlesForUsersAndAllSites(study.getAssignedUsers(), userPermissionsMap);
			final Map<Long, Map<Long, Set<PermissionBundle>>> userSitePermissions = getPermissionBundlesForUsersAndSites(
					study.getAssignedUsers(), userPermissionsMap, study.getSites());

			for (final RandimiUser user : study.getAssignedUsers()) {
				final StudyUserDTO studyUserDTO = new StudyUserDTO(user, userStudyPermissions.get(user.getId()),
				                                                   userAllSitePermissions.get(user.getId()));
				for (final Site site : study.getSites())
					studyUserDTO.getSitePermissionBundles().put(site.getGuiName(),
					                                            userSitePermissions.get(site.getId())
					                                                               .get(user.getId()));
				studyUserDTOs.add(studyUserDTO);
			}
		}
		return studyUserDTOs;
	}

	/**
	 * Maps a single user to a StudyUserDTO.
	 * When mapping an entire study the function {@link #toStudyUserDTOs(Study)} should be used
	 * for less database calls and better performance.
	 *
	 * @param study The study for which the permissions should be mapped.
	 * @param user The user to convert.
	 * @return The mapped StudyUserDTO.
	 */
	public StudyUserDTO toStudyUserDTO(final Study study, final RandimiUser user) {
		if (study != null && study.getId() != 0) {
			final Set<PermissionType> userPermissions = aclEntryRepository.getPermissionTypesByObjectAndUser(study,
			                                                                                                 user);
			final Set<PermissionBundle> userStudyPermissions = getPermissionBundlesForUserAndStudy(userPermissions);
			final Set<PermissionBundle> allSitePermissions = getPermissionBundlesForUserAndAllSites(userPermissions);

			final StudyUserDTO studyUserDTO = new StudyUserDTO(user, userStudyPermissions, allSitePermissions);

			for (final Site site : study.getSites()) {
				final Set<PermissionType> userSitePermissions = aclEntryRepository.getPermissionTypesByObjectAndUser(
						site, user);
				studyUserDTO.getSitePermissionBundles().put(site.getGuiName(),
				                                            getPermissionBundlesForUserAndSite(userPermissions,
				                                                                               userSitePermissions));
			}

			return studyUserDTO;
		} else {
			return new StudyUserDTO(user, new HashSet<>(), new HashSet<>());
		}
	}

	public List<StudyUserDTO> toStudyUserDTO(final Study study, final List<Long> userIds) {
		final List<StudyUserDTO> studyUserDTOs = new ArrayList<>();
		final Iterable<RandimiUser> users = randimiUserRepository.findAllById(userIds);
		for (final RandimiUser user : users)
			studyUserDTOs.add(toStudyUserDTO(study, user));
		return studyUserDTOs;
	}

	public Set<PermissionBundle> getPermissionBundlesForUserAndStudy(final Set<PermissionType> userPermissions) {
		return userPermissions != null
		       ? PermissionBundle.getPermissionBundlesForStudy(userPermissions)
		       : new HashSet<>();
	}

	public Set<PermissionBundle> getPermissionBundlesForUserAndAllSites(@Nullable final Set<PermissionType> userPermissions) {
		return userPermissions != null
		       ? PermissionBundle.getPermissionBundlesForAllSites(userPermissions)
		       : new HashSet<>();
	}

	private Map<Long, Set<PermissionBundle>> getPermissionBundlesForUsersAndStudy(final Set<RandimiUser> users,
	                                                                              final Map<AclSid, Set<PermissionType>> userRightsMap) {
		final Map<Long, Set<PermissionBundle>> userPermissionsMap = new HashMap<>();
		for (final RandimiUser user : users)
			userPermissionsMap.put(user.getId(),
			                       getPermissionBundlesForUserAndStudy(userRightsMap.get(user.getAclSid())));
		return userPermissionsMap;
	}

	public Set<PermissionBundle> getPermissionBundlesForUserAndSite(final Set<PermissionType> userStudyPermissions,
	                                                                final Set<PermissionType> userSitePermissions) {
		return userStudyPermissions != null && userSitePermissions != null
		       ? PermissionBundle.getPermissionBundlesForSite(userSitePermissions, userStudyPermissions)
		       : new HashSet<>();
	}

	private Map<Long, Set<PermissionBundle>> getPermissionBundlesForUsersAndSite(final Set<RandimiUser> users,
	                                                                             final Map<AclSid, Set<PermissionType>> userStudyRightsMap,
	                                                                             final Site site) {
		final Map<Long, Set<PermissionBundle>> userPermissionsMap = new HashMap<>();
		final Map<AclSid, Set<PermissionType>> userSiteRightsMap = aclEntryRepository.getUserRightsByObject(site);
		for (final RandimiUser user : users)
			userPermissionsMap.put(user.getId(),
			                       getPermissionBundlesForUserAndSite(userSiteRightsMap.get(user.getAclSid()),
			                                                          userStudyRightsMap.get(user.getAclSid())));
		return userPermissionsMap;
	}

	private Map<Long, Map<Long, Set<PermissionBundle>>> getPermissionBundlesForUsersAndSites(
			final Set<RandimiUser> users,
			final Map<AclSid, Set<PermissionType>> userStudyRightsMap,
			final List<Site> sites) {
		final Map<Long, Map<Long, Set<PermissionBundle>>> userPermissionsMap = new HashMap<>();
		for (final Site site : sites)
			userPermissionsMap.put(site.getId(), getPermissionBundlesForUsersAndSite(users, userStudyRightsMap, site));
		return userPermissionsMap;
	}

	private Map<Long, Set<PermissionBundle>> getPermissionBundlesForUsersAndAllSites(
			final Set<RandimiUser> users,
			final Map<AclSid, Set<PermissionType>> userRightsMap
	) {
		final Map<Long, Set<PermissionBundle>> userPermissionsMap = new HashMap<>();
		for (final RandimiUser user : users) {
			userPermissionsMap.put(user.getId(),
			                       getPermissionBundlesForUserAndAllSites(userRightsMap.get(user.getAclSid())));
		}

		return userPermissionsMap;
	}
}
