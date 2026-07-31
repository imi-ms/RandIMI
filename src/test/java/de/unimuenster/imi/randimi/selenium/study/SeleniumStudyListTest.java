package de.unimuenster.imi.randimi.selenium.study;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SeleniumStudyListTest extends SeleniumBaseTest {

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
	}

	@Test
	public void testStudy() {
		By statusSelector = getStatusSelector(2);
		By actionsSelector = getActionsSelector(2);

		// Check initial status
		var statusElement = basics.findWebElement(statusSelector, "Status not available");
		assertEquals("Not started", statusElement.getText(), "Unexpected initial status!");

		// Check initial number of actions
		var actions = basics.findSameTypeWebElements(actionsSelector, "No actions available!");
		assertEquals(6, actions.size(), "Unexpected number of actions!");

		// Test mode
		var testButton = actions.get(4);
		var href = testButton.getAttribute("href");
		assertNotNull(href, "Unexpected href!");
		assertTrue(href.contains("/studies/test?id="), "Button is not correct");

		basics.useButton(testButton, "Button to test the study is available");
		statusElement = basics.findWebElement(statusSelector, "Status not available");
		assertEquals("Test", statusElement.getText());

		actions = basics.findSameTypeWebElements(actionsSelector, "No actions available!");
		assertEquals(7, actions.size(), "Unexpected number of actions!");

		// Test reset
		actions = basics.findSameTypeWebElements(actionsSelector, "No actions available!");
		var resetButton = actions.get(4);
		href = resetButton.getAttribute("href");
		assertNotNull(href, "Unexpected href!");
		assertTrue(href.contains("/studies/test?id="), "Button is not correct");

		basics.useButton(resetButton, "Button to reset the study is not available");
		statusElement = basics.findWebElement(statusSelector, "Status not available");
		assertEquals("Test", statusElement.getText());
	}

	@Test
	public void lockStudy() {
		By actionsSelector = getActionsSelector(1);
		By statusSelector = getStatusSelector(1);

		// Check initial status
		var statusElement = basics.findWebElement(statusSelector, "Status not available");
		assertEquals("Active", statusElement.getText(), "Unexpected initial status!");
		// Check initial number of actions
		var actions = basics.findSameTypeWebElements(actionsSelector, "No actions available!");
		assertEquals(7, actions.size(), "Unexpected number of actions!");

		// Lock
		var lockForm = actions.get(4);
		var action = lockForm.getAttribute("action");
		assertNotNull(action, "Unexpected action!");
		assertTrue(action.contains("/studies/lock?id="), "Button is not correct");
		var lockButton = lockForm.findElement(By.tagName("a"));

		basics.useButton(lockButton, "Button to test the study is available");
		basics.waitForStaleness(statusElement);
		statusElement = basics.findWebElement(statusSelector, "Status not available");
		assertEquals("Locked", statusElement.getText());

		actions = basics.findSameTypeWebElements(actionsSelector, "No actions available!");
		assertEquals(7, actions.size(), "Unexpected number of actions!");

		// Unlock
		var unlockForm = actions.get(4);
		action = unlockForm.getAttribute("action");
		assertNotNull(action, "Unexpected action!");
		assertTrue(action.contains("/studies/lock?id="), "Button is not correct");
		var unlockButton = unlockForm.findElement(By.tagName("a"));

		basics.useButton(unlockButton, "Button to test the study is available");
		basics.waitForStaleness(statusElement);
		statusElement = basics.findWebElement(statusSelector, "Status not available");
		assertEquals("Active", statusElement.getText());

		actions = basics.findSameTypeWebElements(actionsSelector, "No actions available!");
		assertEquals(7, actions.size(), "Unexpected number of actions!");
	}

	@Override
	@Test
	public void visibilityTest() {
		final Map<String, By> searchPaths = basics.buildById(
				"studyListHeader",
				"addStudyButton",
				"returnButton",
				"studyOverviewButton",
				"archiveSidebarButton",
				"studiesTable",
				"studyNameColumn",
				"studyApiIdColumn",
				"studyActivationDateColumn",
				"studyActionsColumn"
		);

		final Map<String, WebElement> elements = basics.findWebElements(searchPaths,
		                                                                "Required study management overview elements not found.");
		basics.checkVisibility(elements);

		// Get table entries and check if all expected users are found
		List<WebElement> tableEntries = basics.findSameTypeWebElements(By.tagName("tr"), "Study missing in user overview.");
		assertEquals(5, tableEntries.size(), "The table should have exactly 5 rows for 4 studies and the header.");
	}

	/**
	 * Creates selector for the status of the given row.
	 * @param row Row starting at index 1.
	 * @return The selector.
	 */
	private By getStatusSelector(int row) {
		return By.cssSelector("tbody > tr:nth-child(" + row + ") > td:nth-child(3) > span > span");
	}

	private By getActionsSelector(int row) {
		return By.cssSelector("tbody > tr:nth-child(" + row + ") > td:nth-child(5) > div > *");
	}
}
