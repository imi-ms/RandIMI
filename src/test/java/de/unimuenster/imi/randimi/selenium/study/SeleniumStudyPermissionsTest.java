package de.unimuenster.imi.randimi.selenium.study;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SeleniumStudyPermissionsTest extends SeleniumBaseTest {

	private final String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
	private final String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

	/**
	 * Initialises the driver and basic functions and navigates to edit invitation page.
	 */
	@BeforeEach
	public void setup() {
		// Setup on required page
		basics.login(testUserName, testUserPass);
		By userManagementButton = By.id("manage-studies-button");
		basics.redirect(userManagementButton, "Cannot redirect to study management page.");
		By permissionsButton = By.cssSelector("tbody > tr:nth-child(1) > td:nth-child(5) > div > a:nth-child(4)");
		basics.redirect(permissionsButton, "Cannot redirect to permissions page.");
	}

	@Test
	public void grantStudyWideSitePermissions() {
		goToSitePermissions();

		// Grant all permissions
		var grantAllCheckbox = By.cssSelector("#sitePermissionsTable-allSites > tbody > tr:first-child > td:nth-child(3) > input:first-child");
		basics.useButton(grantAllCheckbox, "Cannot grant all site permissions.");

		var permissionCheckbox = basics.findWebElement(grantAllCheckbox, "Cannot find permission checkbox");
		var checked = permissionCheckbox.getAttribute("checked");
		assertEquals("true", checked, "Checkbox is not checked");

		// Revoke all permissions
		basics.useButton(grantAllCheckbox, "Cannot revoke all site permissions.");

		checked = permissionCheckbox.getAttribute("checked");
		assertNull(checked, "Checkbox is not not checked!");
	}

	@Test
	public void grantAllSitePermissions() {
		goToSitePermissions();

		// Grant all permissions
		var grantAllButton = By.cssSelector("#sitePermissionsTable-allSites > tbody > tr:first-child > td:nth-child(3) > .btn");
		basics.useButton(grantAllButton, "Cannot grant all site permissions.");

		var siteRowsSelector = By.cssSelector("#siteNamesTable > tbody > tr > td:first-child");
		var siteRows = basics.findSameTypeWebElements(siteRowsSelector, "Cannot find rows for switching to the sites.");
		for (WebElement siteRow : siteRows) {
			var onclick = siteRow.getAttribute("onclick");
			assertNotNull(onclick, "No onclick attribute found");
			var siteId = onclick.split("'")[1];

			basics.useButton(siteRow, "Cannot switch to site permissions.");

			var permissionCheckboxSelector = By.cssSelector("#sitePermissionsTable-" + siteId +
			                                                " > tbody > tr:first-child > td:nth-child(3) > input:first-child");
			var permissionCheckbox = basics.findWebElement(permissionCheckboxSelector,
			                                               "Cannot find permission checkbox");
			var checked = permissionCheckbox.getAttribute("checked");
			assertEquals("true", checked, "Checkbox is not checked");
		}

		// Revoke all permissions
		var allSitesButton = By.id("row-allSites");
		basics.useButton(allSitesButton, "Cannot switch to all site permissions.");

		var revokeAllButton = By.cssSelector("#sitePermissionsTable-allSites > tbody > tr:first-child > td:nth-child(3) > .btn:last-child");
		basics.useButton(revokeAllButton, "Cannot revoke all site permissions.");

		for (int siteIndex = 1; siteIndex < siteRows.size(); siteIndex++) {
			var onclick = siteRows.get(siteIndex).getAttribute("onclick");
			assertNotNull(onclick, "No onclick attribute found");
			var siteId = onclick.split("'")[1];

			basics.useButton(siteRows.get(siteIndex), "Cannot switch to site permissions.");

			var permissionCheckboxSelector = By.cssSelector("#sitePermissionsTable-" + siteId + " > tbody > tr:first-child > td:nth-child(3) > input:first-child");
			var permissionCheckbox = basics.findWebElement(permissionCheckboxSelector, "Cannot find permission checkbox");
			var checked = permissionCheckbox.getAttribute("checked");
			assertNull(checked, "Checkbox is not not checked!");
		}
	}

	@Override
	@Test
	public void visibilityTest() {
		final Map<String, By> searchPaths = basics.buildById(
				// Sidebar
				"redirectView",
				"redirectStatistics",
				"redirectEdit",
				"redirectEditUsers",
				"redirectSubjectLists",
				"addUserPopupButton",
				"savePopupButton",
				"cancelButton",
				"usersButton",
				"studyPermissionsButton",
				"sitePermissionsButton",
				// Page content
				"header",
				"userTable"
		);

		final Map<String, WebElement> elements = basics.findWebElements(searchPaths,
		                                                                "Required users permissions overview elements not found.");
		basics.checkVisibility(elements);

		// Study Permissions
		var studyPermissionButtonSelector = By.id("studyPermissionsButton");
		var studyPermissionButton = basics.findWebElement(studyPermissionButtonSelector,
		                                                  "Study permissions button not found!");
		basics.useButton(studyPermissionButton, "Failed to switch to study permissions!");

		final Map<String, By> studyPermissionsSearchPaths = basics.buildById(
				"header",
				"studyPermissionsTable"
		);

		final Map<String, WebElement> studyPermissionElements = basics.findWebElements(studyPermissionsSearchPaths,
		                                                                "Required study permissions overview elements not found.");
		basics.checkVisibility(studyPermissionElements);

		// Site Permissions
		goToSitePermissions();
		final Map<String, By> sitePermissionsSearchPaths = basics.buildById(
				"header",
				"siteNamesTable",
				"sitePermissionsTable-allSites"
		);

		final Map<String, WebElement> sitePermissionElements = basics.findWebElements(sitePermissionsSearchPaths,
		                                                                              "Required site permissions overview elements not found.");
		basics.checkVisibility(sitePermissionElements);

	}

	private void goToSitePermissions() {
		var sitePermissionButtonSelector = By.id("sitePermissionsButton");
		var sitePermissionButton = basics.findWebElement(sitePermissionButtonSelector,
		                                                 "Site permissions button not found!");
		basics.useButton(sitePermissionButton, "Failed to switch to site permissions!");
	}
}
