package de.unimuenster.imi.randimi.service.auth;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectListRepository;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * This class holds the custom permission handler that evaluates randimi's
 * permission types.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Log4j2
@Service("customPermissionEvaluator")
public class CustomPermissionEvaluator implements PermissionEvaluator {

	private final AclClassRepository aclClassRepository;
	private final AclEntryRepository aclEntryRepository;
	private final AclObjectIdentityRepository aclObjectIdentityRepository;
	private final RandimiUserRepository randimiUserRepository;
	private final SiteRepository siteRepository;
	private final SubjectListRepository subjectListRepository;
	private final StratumCodeService stratumCodeService;
	private final StudyRepository studyRepository;

	@Autowired
	public CustomPermissionEvaluator(AclObjectIdentityRepository aclObjectIdentityRepository,
	                                 AclEntryRepository aclEntryRepository, AclClassRepository aclClassRepository,
	                                 RandimiUserRepository randimiUserRepository, SiteRepository siteRepository,
	                                 SubjectListRepository subjectListRepository, StratumCodeService stratumCodeService,
	                                 StudyRepository studyRepository) {
		this.aclObjectIdentityRepository = aclObjectIdentityRepository;
		this.aclEntryRepository = aclEntryRepository;
		this.aclClassRepository = aclClassRepository;
		this.randimiUserRepository = randimiUserRepository;
		this.siteRepository = siteRepository;
		this.subjectListRepository = subjectListRepository;
		this.stratumCodeService = stratumCodeService;
		this.studyRepository = studyRepository;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		if ((authentication == null) || (targetDomainObject == null) || !(permission instanceof String)) {
			return false;
		}
		RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		return aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(targetDomainObject, user.getAclSid(),
		                                                                      PermissionType.valueOf(
				                                                                      (String) permission)) != null;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
	                             Object permission) {
		if ((authentication == null) || (targetId == null) || (targetType == null) || !(permission instanceof String)) {
			return false;
		}
		if (authentication.getPrincipal().equals("anonymousUser")) {
			return false;
		}

		RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		final var aclClass = aclClassRepository.findFirstByClassNameOrSynonym(targetType, targetType);
		AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				aclClass, (Long) targetId);

		return aclEntryRepository.findFirstByAclObjectIdentityAndAclSidAndPermissionType(aclObjectIdentity,
		                                                                                 user.getAclSid(),
		                                                                                 PermissionType.valueOf(
				                                                                                 (String) permission)) != null;
	}

	public boolean hasPermissionUser(@Nullable final Authentication authentication, @Nullable final RandimiUser user) {
		// Not authenticates users have no permissions
		if (authentication == null) {
			return false;
		}

		// Load lazy fields of the current user
		RandimiUser currentUser = ((MyUserDetails) authentication.getPrincipal()).getUser();
		currentUser = randimiUserRepository.findById(currentUser.getId()).orElse(null);
		if (currentUser == null) {
			return false;
		}

		// Admins and user managers are allowed to do everything with users
		if (currentUser.hasUserRole(UserRoles.ROLE_ADMIN) || currentUser.hasUserRole(UserRoles.ROLE_STUDY_MANAGER)) {
			return true;
		}

		// Local manager can new users, other people not
		if (currentUser.hasUserRole(UserRoles.ROLE_LOCAL_MANAGER)) {
			if (user == null) {
				return true;
			}

			return user.getInvitationToken() != null && user.getInvitedByUsername() != null &&
			       user.getInvitedByUsername().equals(currentUser.getUsername());
		}

		return false;
	}

	public boolean hasPermissionUser(@Nullable final Authentication authentication, @Nullable final Long userId) {
		RandimiUser user = null;
		if (userId != null) {
			user = randimiUserRepository.findById(userId).orElse(null);
		}
		return hasPermissionUser(authentication, user);
	}

	public boolean hasPermissionUser(@Nullable final Authentication authentication, @Nullable final UserDTO userDTO) {
		Long userId = null;
		if (userDTO != null) {
			userId = userDTO.getId();
		}
		return hasPermissionUser(authentication, userId);
	}

	public boolean hasPermissionStudy(@Nullable final Authentication authentication, @Nullable final Study study,
	                                  @Nullable final String permission) {
		final var hasPermissions = hasAdminRights(authentication, permission);
		if (hasPermissions != null) {
			return hasPermissions;
		}

		if (study == null) {
			return false;
		}

		if (study.isArchived() || study.isDeleted()) {
			if (!hasPermission(authentication, study, PermissionType.UPDATE_STUDY.name())) {
				return false;
			}
		}

		if (study.isDeleted()) {
			return false;
		}

		return hasPermission(authentication, study.getId(), Study.class.getName(), permission);
	}

	public boolean hasPermissionStudyId(@Nullable final Authentication authentication, @Nullable final Long studyId,
	                                    @Nullable final String permission) {
		Study study = null;
		if (studyId != null) {
			study = studyRepository.findById(studyId).orElse(null);
		}
		return hasPermissionStudy(authentication, study, permission);
	}

	public boolean hasPermissionStudyDto(@Nullable final Authentication authentication,
	                                     @Nullable final StudyDTO studyDTO,
	                                     @Nullable final String permission) {
		Study study = null;
		if (studyDTO != null && studyDTO.getId() != null) {
			study = studyRepository.findById(studyDTO.getId()).orElse(null);
		}
		return hasPermissionStudy(authentication, study, permission);
	}

	public boolean hasPermissionStudyApiId(@Nullable final Authentication authentication,
	                                       @Nullable final String studyApiId, @Nullable final String permission) {
		Study study = null;
		if (studyApiId != null) {
			study = studyRepository.findByApiId(studyApiId).orElse(null);
		}
		return hasPermissionStudy(authentication, study, permission);
	}

	public boolean hasPermissionSite(@Nullable final Authentication authentication,
	                                 @Nullable final Site site,
	                                 @Nullable final String permission) {
		final var hasPermissions = hasAdminRights(authentication, permission);
		if (hasPermissions != null) {
			return hasPermissions;
		}

		if (site == null) {
			return false;
		}

		if (hasPermissionStudy(authentication, site.getStudy(), permission)) {
			return true;
		}

		return hasPermission(authentication, site.getId(), Site.class.getName(), permission);
	}

	public boolean hasPermissionSiteId(@Nullable final Authentication authentication, @Nullable final Long studyId,
	                                   @Nullable final Long siteId, @Nullable final String permission) {
		Site site = null;
		if (studyId != null && siteId != null) {
			site =  siteRepository.findByStudyIdAndId(studyId, siteId).orElse(null);
		}
		return hasPermissionSite(authentication, site, permission);
	}

	public boolean hasPermissionSiteApiId2(@Nullable final Authentication authentication, @Nullable final Long studyId,
	                                      @Nullable final String siteApiId, @Nullable final String permission) {
		Site site = null;
		if (studyId != null && siteApiId != null) {
			site =  siteRepository.findByStudyIdAndApiId(studyId, siteApiId).orElse(null);
		}
		return hasPermissionSite(authentication, site, permission);
	}

	public boolean hasPermissionSiteApiId(@Nullable final Authentication authentication,
	                                      @Nullable final String studyApiId, @Nullable final String siteApiId,
	                                      @Nullable final String permission) {
		Site site = null;
		if (studyApiId != null && siteApiId != null) {
			site =  siteRepository.findByStudyApiIdAndApiId(studyApiId, siteApiId).orElse(null);
		}
		return hasPermissionSite(authentication, site, permission);
	}

	public boolean hasPermissionSiteApiIds(@Nullable final Authentication authentication,
	                                       @Nullable final String studyApiId, final List<String> siteApiIds,
	                                       @Nullable final String permission) {
		for (final String siteApiId : siteApiIds) {
			if (!hasPermissionSiteApiId(authentication, studyApiId, siteApiId, permission)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the use with given authentication has the given permission on the subject list with the given ID.
	 *
	 * @param authentication Authentication of the user.
	 * @param subjectListId  ID of the subject list.
	 * @param permission     Permission to check.
	 * @param allowNull      If the user has permissions when the subjectListId is null.
	 * @return If the user has the permission.
	 */
	public boolean hasPermissionSubjectList(@Nullable final Authentication authentication,
	                                        @Nullable final Long subjectListId,
	                                        @Nullable final String permission, final boolean allowNull) {
		if (subjectListId == null && allowNull) {
			return true;
		}
		return hasPermissionSubjectList(authentication, subjectListId, permission);
	}

	public boolean hasPermissionSubjectList(@Nullable final Authentication authentication, String studyApiId,
	                                        @Nullable final Long subjectListId, @Nullable final String permission,
	                                        final boolean allowNull) {
		if (subjectListId == null && allowNull) {
			return true;
		}

		final Optional<Study> study = studyRepository.findByApiId(studyApiId);
		return study.filter(value -> hasPermissionSubjectList(authentication, subjectListId, permission))
		            .isPresent();
	}

	/**
	 * Checks if the use with given authentication has the given permission on the subject list with the given ID.
	 *
	 * @param authentication Authentication of the user.
	 * @param subjectListId  ID of the subject list.
	 * @param permission     Permission to check.
	 * @return If the user has the permission.
	 */
	public boolean hasPermissionSubjectList(@Nullable final Authentication authentication,
	                                        @Nullable final Long subjectListId, @Nullable final String permission) {
		final var hasPermissions = hasAdminRights(authentication, permission);
		if (hasPermissions != null) {
			return hasPermissions;
		}

		if (subjectListId == null) {
			return false;
		}

		final var subjectListOptional = subjectListRepository.findById(subjectListId);
		if (subjectListOptional.isEmpty()) {
			return false;
		}
		final var subjectList = subjectListOptional.get();

		// Check if the user has study wide permissions
		if (hasPermissionStudy(authentication, subjectList.getStudy(), permission)) {
			return true;
		}

		// If not stratified by site, user is allowed to see all lists
		final var locationStratumOptional = stratumCodeService.getLocationStratumPart(subjectList.getStratumParts());
		if (locationStratumOptional.isEmpty()) {
			return true;
		}
		final var locationStratum = locationStratumOptional.get();

		// Check if the user has the permission for the corresponding site
		return hasPermissionSite(authentication, locationStratum.getSite(), permission);
	}

	@Nullable
	private Boolean hasAdminRights(final Authentication authentication, final String permission) {
		if ((authentication == null) || (permission == null)) {
			return false;
		}
		if (authentication.getPrincipal().equals("anonymousUser")) {
			return false;
		}
		RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		if (user.hasUserRole(UserRoles.ROLE_ADMIN) || (!permission.equals(PermissionType.UPDATE_STUDY_USERS.name()) && user.hasUserRole(UserRoles.ROLE_STUDY_MANAGER))) {
			return true;
		}

		return null;
	}
}
