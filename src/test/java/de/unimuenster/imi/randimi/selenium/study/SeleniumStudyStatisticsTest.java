package de.unimuenster.imi.randimi.selenium.study;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeleniumStudyStatisticsTest extends SeleniumBaseTest {

	private final String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
	private final String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

	@BeforeEach
	public void setup() {
		// Setup on the required page
		basics.login(testUserName, testUserPass);
		By userManagementButton = By.id("manage-studies-button");
		basics.redirect(userManagementButton, "Cannot redirect to study management page.");
		By permissionsButton = By.cssSelector("tbody > tr:nth-child(1) > td:nth-child(5) > div > a:nth-child(2)");
		basics.redirect(permissionsButton, "Cannot redirect to permissions page.");
	}

	@Test
	@Override
	public void visibilityTest() {
		final Map<String, By> searchPaths = basics.buildById(
				// Sidebar -> Navigation
				"redirectView",
				"redirectStatistics",
				"redirectEdit",
				"redirectEditUsers",
				"redirectSubjectLists",
				// Sidebar -> Actions
				"returnButton",
				// Sidebar -> Submenus
				"siteStatisticsButton",
				"strataStatisticsButton",
				// Header
				"studyStatisticsHeader"
		);
		final Map<String, WebElement> elements = basics.findWebElements(searchPaths, "Elements not found.");
		basics.checkVisibility(elements);

		final Map<String, By> siteStatisticsElementIds = basics.buildById(
				"siteStatistics",
				"siteStatisticsFilter",
				"siteChart",
				"siteStatisticsTable"
		);
		final Map<String, WebElement> siteStatisticElements = basics.findWebElements(siteStatisticsElementIds, "site statistic elements not found.");
		basics.checkVisibility(siteStatisticElements);
		basics.findWebElement(By.id("siteChartShowCapacities"), "Capacity toggle not found.");
		basics.findWebElement(By.id("siteChartStack"), "Stack toggle not found.");

		// Change to strata statistics
		var strataStatisticsButtonSelector = By.id("strataStatisticsButton");
		var strataStatisticsButton = basics.findWebElement(strataStatisticsButtonSelector,
		                                                   "Strata statistics button not found!");
		basics.useButton(strataStatisticsButton, "Failed to switch to strata statistics!");

		final Map<String, By> strataStatisticsElementIds = basics.buildById(
				"strataStatistics",
				"strataStatisticsFilter",
				"strataChart",
				"strataStatisticsTable"
		);
		final Map<String, WebElement> strataStatisticElements = basics.findWebElements(strataStatisticsElementIds, "Strata statistic elements not found.");
		basics.checkVisibility(strataStatisticElements);
		basics.findWebElement(By.id("strataChartShowCapacities"), "Capacity toggle not found.");
		basics.findWebElement(By.id("strataChartStack"), "Stack toggle not found.");
	}

	@Test
	public void filterArms() {
		var filterInput = basics.findWebElement(By.cssSelector("#siteStatisticsFilter > input"),
		                                        "Filter input not found!");
		basics.writeInTextfield(filterInput, "Arm");
		basics.useButton(By.cssSelector("[data-randimi-value='First arm of active study - API ID']"), "Could not filter by arm!");

		var header = basics.findSameTypeWebElements(By.cssSelector("#siteStatisticsTable > thead > tr > th"),
		                                   "Table header not found!");
		header = header.stream().filter(WebElement::isDisplayed).toList();

		assertEquals(3, header.size());
		assertEquals("", header.get(0).getText(), "Unexpected header text!");
		assertEquals("First arm of active study", header.get(1).getText(), "Unexpected header text!");
		assertEquals("All Study Arms", header.get(2).getText(), "Unexpected header text!");

		basics.useButton(By.className("randimi-autocomplete-selected-item-icon"), "Could not remove filter!");

		header = basics.findSameTypeWebElements(By.cssSelector("#siteStatisticsTable > thead > tr > th"),
		                                        "Table header not found!");
		header = header.stream().filter(WebElement::isDisplayed).toList();
		assertEquals(4, header.size());
	}

	@Test
	public void filterSites() {
		var filterInput = basics.findWebElement(By.cssSelector("#siteStatisticsFilter > input"),
		                                        "Filter input not found!");
		basics.writeInTextfield(filterInput, "Site");
		basics.useButton(By.cssSelector("[data-randimi-value='First Site of active study']"), "Could not filter by site!");

		var header = basics.findSameTypeWebElements(By.cssSelector("#siteStatisticsTable > tbody > * > td:first-child"),
		                                            "Table rows not found!");
		header = header.stream().filter(WebElement::isDisplayed).toList();

		assertEquals(2, header.size());
		assertEquals("First Site of active study", header.get(0).getText(), "Unexpected row!");
		assertEquals("All Sites", header.get(1).getText(), "Unexpected row!");

		basics.useButton(By.className("randimi-autocomplete-selected-item-icon"), "Could not remove filter!");

		header = basics.findSameTypeWebElements(By.cssSelector("#siteStatisticsTable > tbody > * > td:first-child"),
		                                            "Table rows not found!");
		header = header.stream().filter(WebElement::isDisplayed).toList();
		assertEquals(3, header.size());
	}
}
