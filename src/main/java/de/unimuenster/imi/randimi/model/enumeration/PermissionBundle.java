package de.unimuenster.imi.randimi.model.enumeration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enumeration that holds bundles of @link{PermissionType PermissionTypes} to
 * simplify the permissions.
 *
 * @author Tobias Hardt
 */
public enum PermissionBundle {
	MANAGE_PERMISSIONS("MANAGE_PERMISSIONS"),
	MANAGE_STUDY("MANAGE_STUDY"),
	MANAGE_SUBJECTS("MANAGE_SUBJECTS"),
	MONITOR_STUDY("MONITOR_STUDY"),
	RANDOMIZE_SUBJECTS("RANDOMIZE_SUBJECTS"),
	RECEIVE_NOTIFICATIONS("RECEIVE_NOTIFICATIONS");

	private final String textValue;
	private static final Map<String, PermissionBundle> stringToEnum = new HashMap<>();

	// Lists with permissions equal to the bundle
	private static final List<PermissionType> managePermissionsPermissionTypes = Arrays.asList(PermissionType.READ_STUDY,
	                                                                                           PermissionType.UPDATE_STUDY_USERS);
	private static final List<PermissionType> manageStudyPermissionTypes = Arrays.asList(PermissionType.READ_STUDY,
	                                                                                     PermissionType.UPDATE_STUDY,
	                                                                                     PermissionType.DELETE_STUDY,
	                                                                                     PermissionType.READ_AUDIT_COMPLEX);
	private static final List<PermissionType> manageSubjectsPermissionTypes = Arrays.asList(PermissionType.READ_STUDY,
	                                                                                        PermissionType.READ_SUBJECT,
	                                                                                        PermissionType.UPDATE_SUBJECT,
	                                                                                        PermissionType.DELETE_SUBJECT);
	private static final List<PermissionType> monitorStudyPermissionTypes = Arrays.asList(PermissionType.READ_AUDIT_SIMPLE,
	                                                                                      PermissionType.READ_REPORT,
	                                                                                      PermissionType.READ_STUDY);
	private static final List<PermissionType> randomizeSubjectsPermissionTypes = Arrays.asList(PermissionType.CREATE_SUBJECT,
	                                                                                           PermissionType.READ_SUBJECT,
	                                                                                           PermissionType.READ_AUDIT_SIMPLE,
	                                                                                           PermissionType.READ_STUDY);
	private static final List<PermissionType> receiveNotificationPermissionTypes = Arrays.asList(PermissionType.GET_NOTIFICATION);

	// List for conversion
	private static final List<PermissionType> randomizerSitePermissions = Arrays.asList(PermissionType.CREATE_SUBJECT,
	                                                                                    PermissionType.READ_SUBJECT);

	static // Initialize map from constant name to enum constant
	{
		for (PermissionBundle cValue : values()) {
			stringToEnum.put(cValue.toString(), cValue);
		}
	}

	PermissionBundle(String textValue) {
		this.textValue = textValue;
	}

	@Override
	public String toString() {
		return textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static PermissionBundle fromString(String textValue) {
		return stringToEnum.get(textValue);
	}

	public static Set<PermissionType> getPermissionTypes(PermissionBundle permissionBundle) {
		Set<PermissionType> permissionTypes = new HashSet<>();

		switch (permissionBundle) {
			case MANAGE_PERMISSIONS:
				permissionTypes.addAll(managePermissionsPermissionTypes);
				break;
			case MANAGE_STUDY:
				permissionTypes.addAll(manageStudyPermissionTypes);
				break;
			case MANAGE_SUBJECTS:
				permissionTypes.addAll(manageSubjectsPermissionTypes);
				break;
			case MONITOR_STUDY:
				permissionTypes.addAll(monitorStudyPermissionTypes);
				break;
			case RANDOMIZE_SUBJECTS:
				permissionTypes.addAll(randomizeSubjectsPermissionTypes);
				break;
			case RECEIVE_NOTIFICATIONS:
				permissionTypes.addAll(receiveNotificationPermissionTypes);
				break;
			default:
				break;
		}
		return permissionTypes;
	}

	public static Set<PermissionBundle> getPermissionBundlesForStudy(Set<PermissionType> permissionTypes) {
		Set<PermissionBundle> permissionBundles = new HashSet<>();
		if (permissionTypes.containsAll(managePermissionsPermissionTypes))
			permissionBundles.add(MANAGE_PERMISSIONS);
		if (permissionTypes.containsAll(manageStudyPermissionTypes))
			permissionBundles.add(MANAGE_STUDY);
		if (permissionTypes.containsAll(manageSubjectsPermissionTypes))
			permissionBundles.add(MANAGE_SUBJECTS);
		if (permissionTypes.containsAll(monitorStudyPermissionTypes))
			permissionBundles.add(MONITOR_STUDY);
		if (permissionTypes.containsAll(receiveNotificationPermissionTypes))
			permissionBundles.add(RECEIVE_NOTIFICATIONS);
		return permissionBundles;
	}

	public static Set<PermissionBundle> getPermissionBundlesForSite(final Set<PermissionType> sitePermissionTypes,
	                                                                final Set<PermissionType> studyPermissionTypes) {
		Set<PermissionBundle> permissionBundles = new HashSet<>();

		final Set<PermissionType> permissionTypes = new HashSet<>(sitePermissionTypes);
		permissionTypes.addAll(studyPermissionTypes);

		if (permissionTypes.containsAll(randomizerSitePermissions))
			permissionBundles.add(RANDOMIZE_SUBJECTS);
		return permissionBundles;
	}

	public static Set<PermissionBundle> getPermissionBundlesForAllSites(final Set<PermissionType> permissionTypes) {
		final Set<PermissionBundle> permissionBundles = new HashSet<>();
		if (permissionTypes.containsAll(randomizerSitePermissions))
			permissionBundles.add(RANDOMIZE_SUBJECTS);
		return permissionBundles;
	}

	public static Set<PermissionBundle> getStudyPermissionBundles() {
		return Set.of(MANAGE_PERMISSIONS, MANAGE_STUDY, MANAGE_SUBJECTS, MONITOR_STUDY, RECEIVE_NOTIFICATIONS);
	}

	public static Set<PermissionBundle> getSitePermissionBundles() {
		return Set.of(RANDOMIZE_SUBJECTS);
	}
}
