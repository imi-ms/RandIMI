package de.unimuenster.imi.randimi.selenium.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.List;

import de.unimuenster.imi.randimi.TestCacheConfig;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumLanguageAutomation;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumTextfieldPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests for the settings page.
 * Url: "/settings/edit"
 *
 * @author Anika Herbermann
 */
@ContextConfiguration(classes = TestCacheConfig.class)
public class SeleniumSettingsTest extends SeleniumBaseTest {

	private String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
	private String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

	private int numberFalseTextfieldElements;
	private SeleniumTextfieldPair[] falseTextfieldElements;
	private By[] falseCheckbox;
	private String currentLanguageValue;

	/**
	 * Initialises the driver and basic functions and navigates to settings page.
	 */
	@BeforeEach
	public void setup() {
		// Setup on required page
		basics.login(testUserName, testUserPass);
		By settingsButton = By.id("settings-button");
		basics.redirect(settingsButton, "Cannot redirect to settings page.");
	}

	@Test
	public void changeDefaultLanguageTest() {
		// Finding a new language
		Select languageSelection = new Select(basics.findWebElement(By.id("defaultLanguage"), "No selection found."));

		String currentLanguage = languageSelection.getFirstSelectedOption().getAttribute("value");

		List<WebElement> languages = languageSelection.getOptions();
		Iterator<WebElement> iterator = languages.iterator();

		String newLanguageValue = "";
		WebElement newLanguage = null;

		while (iterator.hasNext() && newLanguageValue.isBlank()) {
			newLanguage = iterator.next();
			String currentValue = newLanguage.getAttribute("value");

			if (!currentValue.equals(currentLanguage)) {
				newLanguageValue = currentValue;
			}
		}

		if (newLanguageValue.isBlank()) {
			fail("Other languages missing.");
		}

		// Selecting new language
		languageSelection.selectByValue(newLanguageValue);

		// Saving changes
		basics.redirect(By.id("saveButton"), "Save button not usable.");

		// Reloading page
		basics.redirect(By.id("settings-button"), "Could not navigate to settings.");

		// Searching for changes
		languageSelection = new Select(basics.findWebElement(By.id("defaultLanguage"), "No selection found."));
		String selectedLanguage = languageSelection.getFirstSelectedOption().getAttribute("value");

		assertEquals(newLanguageValue, selectedLanguage, "The new default language was not saved correctly.");
	}

	/**
	 * Tests whether multiple templates can be created.
	 */
	@Test
	public void pseudonymTemplateCreationTest() {
		// New values
		int numberNewTemplates = 2;
		String[][] input = new String[numberNewTemplates][3];
		input[0][0] = "Patient with number";
		input[0][1] = "Give away a three digit number for every patient.";
		input[0][2] = "Patient [0-9]{3}";
		input[1][0] = "Patient identification number";
		input[1][1] = "Give patient a six digit long id.";
		input[1][2] = "[0-9]{6}";

		List<WebElement> templates = getPseudonymRegexTemplates();
		final int numberOldTemplates = templates.size();

		// Creating tow new templates
		basics.useButton(By.id("addPseudonymRegexButton"), "Add regex button not usable.");
		basics.useButton(By.id("addPseudonymRegexButton"), "Add regex button not usable.");

		// Insert values into new templates
		templates = getPseudonymRegexTemplates();
		assertEquals(numberOldTemplates + numberNewTemplates, templates.size(), "New templates were not added!");

		String failMessage = "";
		for (int i = 0; i < numberNewTemplates; i++) {
			final int orderNumber =  numberOldTemplates + i;
			final String idPrefix = "pseudonymRegexList" + orderNumber + ".";
			for (int j = 0; j < SupportedLanguage.values().length; ++j) {
				final String idPrefixInner = idPrefix + "pseudonymRegexDescriptionDTOList" + j + ".";
				basics.useButton(By.id("pseudonymRegex" + orderNumber + "-" + j + "Button"), "Unable to switch description language");

				failMessage = "No template name found.";
				WebElement currentName = basics.findWebElement(By.id(idPrefixInner + "name"), failMessage);
				basics.waitForVisibility(currentName);
				basics.writeInTextfield(currentName, input[i][0] + " Lang index: " + j);

				failMessage = "No template description found.";
				WebElement currentDescription = basics.findWebElement(By.id(idPrefixInner + "description"), failMessage);
				basics.writeInTextfield(currentDescription, input[i][1]);
			}

			failMessage = "No template regex found.";
			WebElement currentRegex = basics.findWebElement(By.id(idPrefix + "regex"), failMessage);
			basics.writeInTextfield(currentRegex, input[i][2]);
		}

		// Saving values
		String emailPass = SeleniumPropUtils.getProperty("testEmail.pass");
		SeleniumTextfieldPair password = new SeleniumTextfieldPair(By.id("mailPassword"), emailPass);
		basics.findWebElementAndWrite(password, "Could not write password for email.");

		basics.redirect(By.id("saveButton"), "Save button not usable.");

		// Reloading page
		basics.redirect(By.id("settings-button"), "Could not navigate to settings.");

		// Checking saved values
		templates = getPseudonymRegexTemplates();
		assertEquals(numberOldTemplates + numberNewTemplates, templates.size(), "New templates were not added!");

		for (int i = 0; i < numberNewTemplates; i++) {
			final int orderNumber = numberOldTemplates + i;
			final String idPrefix = "pseudonymRegexList" + orderNumber + ".";
			basics.useButton(By.id("pseudonymRegex" + orderNumber + "-" + 0 + "Button"), "Unable to switch description language");
			for (int j = 0; j < SupportedLanguage.values().length; ++j) {
				final String idPrefixInner = idPrefix + "pseudonymRegexDescriptionDTOList" + j + ".";
				basics.useButton(By.id("pseudonymRegex" + orderNumber + "-" + j + "Button"), "Unable to switch description language");

				WebElement currentName = basics.findWebElement(By.id(idPrefixInner + "name"), failMessage);
				basics.waitForVisibility(currentName);
				assertEquals(input[i][0] + " Lang index: " + j, currentName.getAttribute("value"), "Template name was not saved properly.");

				WebElement currentDescription = basics.findWebElement(By.id(idPrefixInner + "description"), failMessage);
				assertEquals(input[i][1], currentDescription.getText(), "Template description was not saved properly.");
			}

			WebElement currentRegex = basics.findWebElement(By.id(idPrefix + "regex"), failMessage);
			assertEquals(input[i][2], currentRegex.getAttribute("value"), "Template regex was not saved properly.");
		}
	}

	/**
	 * Tests, whether a template can be changed.
	 */
	@Test
	public void pseudonymTemplateChangeTest() {
		// Values of template that shall be changed
		String name = SeleniumPropUtils.getProperty("testRegex.name");
		String description = SeleniumPropUtils.getProperty("testRegex.description");
		String regex = SeleniumPropUtils.getProperty("testRegex.regex");

		// Values for change
		String newName = "New test regex";
		String newDescription = "New description";
		String newRegex = "[A-Z]{3}";

		// Finding the template
		List<WebElement> templates = getPseudonymRegexTemplates();
		final int numberTemplates = templates.size();

		WebElement currentName = null;
		WebElement currentDescription = null;
		WebElement currentRegex = null;

		boolean found = false;
		for (int i = 0; i < numberTemplates && !found; i++) {
			final String idPrefix = "pseudonymRegexList" + i + ".";
			final String idPrefixInner = idPrefix + "pseudonymRegexDescriptionDTOList" + 1 + ".";

			basics.useButton(By.id("pseudonymRegex" + i + "-" + 1 + "Button"), "Unable to switch description language");

			String failMessage = "No template name found.";
			currentName = basics.findWebElement(By.id(idPrefixInner + "name"), failMessage);
			basics.waitForVisibility(currentName);

			failMessage = "No template description found.";
			currentDescription = basics.findWebElement(By.id(idPrefixInner + "description"), failMessage);

			failMessage = "No template regex found.";
			currentRegex = basics.findWebElement(By.id(idPrefix + "regex"), failMessage);

			final String n = currentName.getAttribute("value");
			final String d = currentDescription.getText();
			final String r = currentRegex.getAttribute("value");

			if (currentName.getAttribute("value").equals(name) && currentDescription.getText().equals(description) &&
					currentRegex.getAttribute("value").equals(regex)) {
				found = true;
			}
		}

		assertTrue(found, "Template not found");

		// Changing the template
		basics.writeInTextfield(currentName, newName);
		basics.writeInTextfield(currentDescription, newDescription);
		basics.writeInTextfield(currentRegex, newRegex);

		// Saving values
		basics.redirect(By.id("saveButton"), "Save button not usable.");
		basics.findWebElement(By.id("successDiv"), "Success message not found.");
		basics.doNotFindWebElement(By.id("errorDiv"), "Error message found.");

		// Reloading page
		basics.redirect(By.id("settings-button"), "Could not navigate to settings.");

		// Searching for changed template
		boolean foundOld = false;
		boolean foundNew = false;
		for (int i = 0; i < numberTemplates; i++) {
			final String idPrefix = "pseudonymRegexList" + i + ".";
			final String idPrefixInner = idPrefix + "pseudonymRegexDescriptionDTOList" + 1 + ".";

			basics.useButton(By.id("pseudonymRegex" + i + "-" + 1 + "Button"), "Unable to switch description language");

			String failMessage = "No template name found.";
			currentName = basics.findWebElement(By.id(idPrefixInner + "name"), failMessage);
			basics.waitForVisibility(currentName);

			failMessage = "No template description found.";
			currentDescription = basics.findWebElement(By.id(idPrefixInner + "description"), failMessage);

			failMessage = "No template regex found.";
			currentRegex = basics.findWebElement(By.id(idPrefix + "regex"), failMessage);

			final String n = currentName.getAttribute("value");
			final String d = currentDescription.getText();
			final String r = currentRegex.getAttribute("value");

			// Checking if new template is correctly saved
			if (currentName.getAttribute("value").equals(newName) && currentDescription.getText().equals(newDescription) &&
					currentRegex.getAttribute("value").equals(newRegex)) {
				foundNew = true;
			}

			// Checking if (part of the) old template is still present
			if (currentName.getAttribute("value").equals(name) || currentDescription.getText().equals(description) ||
					currentRegex.getAttribute("value").equals(regex)) {
				foundOld = true;
			}
		}

		assertFalse(foundOld, "Values of the unchanged template are still present.");
		assertTrue(foundNew, "The changed template can not be found.");
	}

	/**
	 * Tests, wether a template can be deleted.
	 */
	@Test
	public void pseudonymTemplateDeletionTest() {
		// Values of template that shall be deleted
		String name = SeleniumPropUtils.getProperty("testRegex.name");
		String description = SeleniumPropUtils.getProperty("testRegex.description");
		String regex = SeleniumPropUtils.getProperty("testRegex.regex");

		// Finding the template
		List<WebElement> templates = getPseudonymRegexTemplates();
		int numberTemplates = templates.size();

		WebElement currentName = null;
		WebElement currentDescription = null;
		WebElement currentRegex = null;

		boolean found = false;
		int templateIndex = 0;
		for (int i = 0; i < numberTemplates && !found; i++) {
			final String idPrefix = "pseudonymRegexList" + i + ".";
			final String idPrefixInner = idPrefix + "pseudonymRegexDescriptionDTOList" + 1 + ".";

			basics.useButton(By.id("pseudonymRegex" + i + "-" + 1 + "Button"), "Unable to switch description language");

			String failMessage = "No template name found.";
			currentName = basics.findWebElement(By.id(idPrefixInner + "name"), failMessage);
			basics.waitForVisibility(currentName);

			failMessage = "No template description found.";
			currentDescription = basics.findWebElement(By.id(idPrefixInner + "description"), failMessage);

			failMessage = "No template regex found.";
			currentRegex = basics.findWebElement(By.id(idPrefix + "regex"), failMessage);

			final String n = currentName.getAttribute("value");
			final String d = currentDescription.getText();
			final String r = currentRegex.getAttribute("value");

			if (currentName.getAttribute("value").equals(name) && currentDescription.getText().equals(description) &&
					currentRegex.getAttribute("value").equals(regex)) {
				found = true;
				templateIndex = i;
			}
		}

		assertTrue(found, "Template not found");

		// Removing the template
		final WebElement currentTemplate = templates.get(templateIndex);
		WebElement removeButton = basics.findWebElementWithin(currentTemplate, By.className("removePseudonymRegex"), "No remove template button found.");
		basics.useButton(removeButton, "Remove button not usable.");

		// Saving values
		String emailPass = SeleniumPropUtils.getProperty("testEmail.pass");
		SeleniumTextfieldPair password = new SeleniumTextfieldPair(By.id("mailPassword"), emailPass);
		basics.findWebElementAndWrite(password, "Could not write password for email.");

		basics.redirect(By.id("saveButton"), "Save button not usable.");

		// Reloading page
		basics.redirect(By.id("settings-button"), "Could not navigate to settings.");

		// Searching for an incorrectly saved template
		templates = getPseudonymRegexTemplates();
		numberTemplates = templates.size();

		if (templates.size() <= 0) {
			fail("No templates left after deletion of only one template.");
		}

		found = false;
		for (int i = 0; i < numberTemplates && !found; i++) {
			final String idPrefix = "pseudonymRegexList" + i + ".";
			final String idPrefixInner = idPrefix + "pseudonymRegexDescriptionDTOList" + 1 + ".";

			basics.useButton(By.id("pseudonymRegex" + i + "-" + 1 + "Button"), "Unable to switch description language");

			String failMessage = "No template name found.";
			currentName = basics.findWebElement(By.id(idPrefixInner + "name"), failMessage);
			basics.waitForVisibility(currentName);

			failMessage = "No template description found.";
			currentDescription = basics.findWebElement(By.id(idPrefixInner + "description"), failMessage);

			failMessage = "No template regex found.";
			currentRegex = basics.findWebElement(By.id(idPrefix + "regex"), failMessage);

			if (currentName.getAttribute("value").equals(name) || currentDescription.getText().equals(description) ||
					currentRegex.getAttribute("value").equals(regex)) {
				found = true;
			}
		}

		assertFalse(found, "The deleted template should not exist after removal.");
	}

	/**
	 * Tests, whether all email components can be changed and then saved correctly.
	 */
	@Test
	public void emailChangeTest() {
		// New values
		String host = "mail.uni-muenster.de";
		String port = "25";
		String mail = "rand1m1@wwu.de";
		String username = "random";
		String password = "changeme";

		// Insert values into textfields
		int numberTextfields = 5;
		SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[numberTextfields];
		pairs[0] = new SeleniumTextfieldPair(By.id("mailHost"), host);
		pairs[1] = new SeleniumTextfieldPair(By.id("mailHost"), host);
		// TODO Changing the port results in an error
		//pairs[1] = new SeleniumTextfieldPair(By.id("mailPort"), port);
		pairs[2] = new SeleniumTextfieldPair(By.id("mailUsername"), username);
		pairs[3] = new SeleniumTextfieldPair(By.id("mailPassword"), password);
		pairs[4] = new SeleniumTextfieldPair(By.id("mailSender"), mail);

		basics.findWebElementsAndWrite(pairs, "Could not use email textfields.");

		// Change checkboxes
		basics.useButton(By.id("mailTLS"), "Mail TLS checkbox not usable.");
		basics.useButton(By.id("mailSMTPAuth"), "Mail SMTP checkbox not usable.");

		// Save changes
		basics.redirect(By.id("saveButton"), "Save button not usable.");
		basics.findWebElement(By.id("successDiv"), "Changes not saved.");
		basics.doNotFindWebElement(By.id("errorDiv"), "There are some errors.");

		// Reloading page
		basics.redirect(By.id("settings-button"), "Could not navigate to settings.");

		// Find textfields
		WebElement[] elements = basics.findWebElements(pairs, "Textfields not found.");
		WebElement tlsCheckbox = basics.findWebElement(By.id("mailTLS"), "Mail TLS checkbox not found.");
		WebElement smtpCheckbox = basics.findWebElement(By.id("mailSMTPAuth"), "Mail SMTP checkbox not found.");

		// Check for new values
		assertEquals(host, elements[0].getAttribute("value"), "Host was not saved correctly.");
		//assertEquals(port, elements[1].getAttribute("value"), "Port was not saved correctly.");
		assertEquals(username, elements[2].getAttribute("value"), "Username was not saved correctly.");
		assertEquals(mail, elements[4].getAttribute("value"), "Mail sender was not saved correctly.");

		assertFalse(tlsCheckbox.isSelected(), "The TLS checkbox should not be selected.");
		assertFalse(smtpCheckbox.isSelected(), "The SMTP checkbox should not be selected.");
	}

	/**
	 * Tests, whether all support elements can be changed and then saved.
	 */
	@Test
	public void configureSupportTest() {
		// New values
		String mail = "rand1m1@wwu.de";
		String phone = "+49 0000000000";

		// Insert values into textfields
		int numberTextfields = 2;
		SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[numberTextfields];
		pairs[0] = new SeleniumTextfieldPair(By.id("supportMail"), mail);
		pairs[1] = new SeleniumTextfieldPair(By.id("supportPhone"), phone);

		basics.findWebElementsAndWrite(pairs, "Could not use support textfields.");

		// Save changes
		basics.redirect(By.id("saveButton"), "Save button not usable.");

		// Reloading page
		basics.redirect(By.id("settings-button"), "Could not navigate to settings.");

		// Find textfields
		WebElement[] elements = basics.findWebElements(pairs, "Textfields not found.");

		// Check for new values
		assertEquals(mail, elements[0].getAttribute("value"), "Support mail was not saved correctly.");
		assertEquals(phone, elements[1].getAttribute("value"), "Support phone number was not saved correctly.");
	}

	@Test
	public void changeColorsTest() {
		// New colors
		String newMainColor = "#79b997";
		String newHighlightColor = "#79b1b9";

		// Insert values into selection textfields
		SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[2];
		pairs[0] = new SeleniumTextfieldPair(By.id("mainColor"), newMainColor);
		pairs[1] = new SeleniumTextfieldPair(By.id("highlightColor"), newHighlightColor);

		basics.findWebElementsAndWrite(pairs, "Color textfields not usable.");

		// Save changes
		String emailPass = SeleniumPropUtils.getProperty("testEmail.pass");
		SeleniumTextfieldPair password = new SeleniumTextfieldPair(By.id("mailPassword"), emailPass);
		basics.findWebElementAndWrite(password, "Could not write password for email.");

		basics.redirect(By.id("saveButton"), "Save button not usable.");

		// Reloading page
		basics.redirect(By.id("settings-button"), "Could not navigate to settings.");

		// Find textfields
		WebElement[] elements = basics.findWebElements(pairs, "Textfields not found.");

		// Check for new values
		assertEquals(newMainColor, elements[0].getAttribute("value"), "New main color was not saved correctly.");
		assertEquals(newHighlightColor, elements[1].getAttribute("value"), "New highlight color was not saved correctly.");
	}

	/**
	 * Checks if all specific elements are visible on the settings page.
	 */
	@Test
	public void visibilityTest() {
		// Preperation and search for other HTML elements
		int numberElements = 34;
		By[] searchPaths = new By[numberElements];

		// Head
		searchPaths[0] = By.id("headline");
		// Language
		searchPaths[1] = By.id("languageFieldset");
		searchPaths[2] = By.id("defaultLanguageLabel");
		searchPaths[3] = By.id("defaultLanguage");
		// Mail
		searchPaths[4] = By.id("mailFieldset");
		searchPaths[5] = By.id("mailHostLabel");
		searchPaths[6] = By.id("mailHost");
		searchPaths[7] = By.id("mailPortLabel");
		searchPaths[8] = By.id("mailPort");
		searchPaths[9] = By.id("mailTLS");
		searchPaths[10] = By.id("mailTLSLabel");
		searchPaths[11] = By.id("mailSMTPAuth");
		searchPaths[12] = By.id("mailSMTPAuthLabel");
		searchPaths[13] = By.id("mailUsername");
		searchPaths[14] = By.id("mailUsernameLabel");
		searchPaths[15] = By.id("mailPassword");
		searchPaths[16] = By.id("mailPasswordLabel");
		searchPaths[17] = By.id("mailSender");
		searchPaths[18] = By.id("mailSenderLabel");
		// Support
		searchPaths[19] = By.id("supportFieldset");
		searchPaths[20] = By.id("supportMailLabel");
		searchPaths[21] = By.id("supportMail");
		searchPaths[22] = By.id("supportPhoneLabel");
		searchPaths[23] = By.id("supportPhone");
		// Colors
		searchPaths[24] = By.id("customizeFieldset");
		searchPaths[25] = By.id("mainColorLabel");
		searchPaths[26] = By.id("mainColor");
		searchPaths[27] = By.id("highlightColorLabel");
		searchPaths[28] = By.id("highlightColor");
		searchPaths[29] = By.id("resetColors");
		// Buttons
		searchPaths[30] = By.id("saveButton");
		searchPaths[31] = By.id("cancelButton");
		// Template section
		searchPaths[32] = By.id("pseudonymTemplatesFieldset");
		searchPaths[33] = By.id("addPseudonymRegexButton");


		WebElement[] elements = basics.findWebElements(searchPaths, "Required profile elements not found.");

		// Check, if all elements can be seen
		String invisibleElements = "";
		for (int i = 0; i < numberElements; i++) {
			if (!elements[i].isDisplayed()) {
				if (invisibleElements.equals("")) {
					invisibleElements = searchPaths[i].toString();
				} else {
					invisibleElements += "', '" + searchPaths[i];
				}
			}
		}

		// Visibility of template elements
		basics.useButton(elements[33], "Add template button not usable.");

		int innerTemplateElements = 8;
		searchPaths = new By[innerTemplateElements];
		searchPaths[0] = By.className("pseudonymRegexTemplateFieldset");
		searchPaths[1] = By.id("templateNameLabel");
		searchPaths[2] = By.cssSelector("div[id='templateNameDiv']>input");
		searchPaths[3] = By.id("templateDescriptionLabel");
		searchPaths[4] = By.cssSelector("div[id='templateDescriptionDiv']>textarea");
		searchPaths[5] = By.id("templateRegexLabel");
		searchPaths[6] = By.cssSelector("div[id='templateRegexDiv']>input");
		searchPaths[7] = By.className("removePseudonymRegex");

		WebElement templates = basics.findWebElement(By.id("pseudonymTemplatesFieldset"),
		                                             "Unable to find fieldset of pseudonym regex templates");
		elements = basics.findWebElementsWithin(templates, searchPaths, "Inner template elements not found.");

		for (int i = 0; i < innerTemplateElements; i++) {
			if (!elements[i].isDisplayed()) {
				if (invisibleElements.equals("")) {
					invisibleElements = searchPaths[i].toString();
				} else {
					invisibleElements += "', '" + searchPaths[i];
				}
			}
		}

		// Check if all languages can be selected
		boolean missing = false;
		int numberLanguages = Integer.parseInt(SeleniumPropUtils.getProperty("language.number"));
		List<WebElement> options = basics.findSameTypeWebElements(By.cssSelector("select[id='defaultLanguage']>option"), "No options found.");
		if (options.size() != numberLanguages) {
			missing = true;
		}

		// Output
		String output = "";
		if (missing) {
			output += "There are language options missing in settings. ";
		}
		if (!invisibleElements.equals("")) {
			output += "Elements with search paths '" + invisibleElements + "' cannot be seen. ";
		}
		if (!output.isEmpty()) {
			fail(output);
		}
	}

	/**
	 * Checks all errormessages for required textfields and their translations.
	 */
	@Test
	public void missingInputTest() {
		// Setting up input
		String email = SeleniumPropUtils.getProperty("testEmail");
		String emailUsername = SeleniumPropUtils.getProperty("testEmail.name");
		String emailPassword = SeleniumPropUtils.getProperty("testEmail.pass");
		String emailSystemHost = SeleniumPropUtils.getProperty("testEmail.host");
		String emailSystemPort = SeleniumPropUtils.getProperty("testEmail.port");
		String emailTel = SeleniumPropUtils.getProperty("testEmail.tel");

		// Preparing textfields
		int numberTextfields = 7;
		SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[numberTextfields];
		pairs[0] = new SeleniumTextfieldPair(By.id("mailHost"), emailSystemHost);
		pairs[1] = new SeleniumTextfieldPair(By.id("mailPort"), emailSystemPort);
		pairs[2] = new SeleniumTextfieldPair(By.id("mailUsername"), emailUsername);
		pairs[3] = new SeleniumTextfieldPair(By.id("mailPassword"), emailPassword);
		pairs[4] = new SeleniumTextfieldPair(By.id("mailSender"), email);

		pairs[5] = new SeleniumTextfieldPair(By.id("supportMail"), email);
		pairs[6] = new SeleniumTextfieldPair(By.id("supportPhone"), emailTel);

		// Starting test
		SeleniumLanguageAutomation auto = new SeleniumLanguageAutomation(basics);
		auto.checkErrorMessages("error", pairs, By.id("saveButton"));
	}

	/**
	 * Inserts or selects values on the settings page, that should not be saved on cancel.
	 */
	public void prepareFalseInput() {
		// Input values
		String falseEmailSender = "rand1m1@wwu.de";
		String falseEmailUsername = "falseName";
		String falseEmailPassword = "falsePass";
		String falseEmailSystemHost = "secmail.wwu.de";
		String falseEmailSystemPort = "578";
		String falseSupportEmail = "randimi@wwu.de";
		String falseSupportPhone = "+94";
		String falseMainColor = "#BF110D";
		String falseHighlightColor = "#001B63";

		// Inserting textfield input
		numberFalseTextfieldElements = 9;
		falseTextfieldElements = new SeleniumTextfieldPair[numberFalseTextfieldElements];
		falseTextfieldElements[0] = new SeleniumTextfieldPair(By.id("mailHost"), falseEmailSystemHost);
		falseTextfieldElements[1] = new SeleniumTextfieldPair(By.id("mailPort"), falseEmailSystemPort);
		falseTextfieldElements[2] = new SeleniumTextfieldPair(By.id("mailUsername"), falseEmailUsername);
		falseTextfieldElements[3] = new SeleniumTextfieldPair(By.id("mailPassword"), falseEmailPassword);
		falseTextfieldElements[4] = new SeleniumTextfieldPair(By.id("mailSender"), falseEmailSender);
		falseTextfieldElements[5] = new SeleniumTextfieldPair(By.id("supportMail"), falseSupportEmail);
		falseTextfieldElements[6] = new SeleniumTextfieldPair(By.id("supportPhone"), falseSupportPhone);
		falseTextfieldElements[7] = new SeleniumTextfieldPair(By.id("mainColor"), falseMainColor);
		falseTextfieldElements[8] = new SeleniumTextfieldPair(By.id("highlightColor"), falseHighlightColor);

		basics.findWebElementsAndWrite(falseTextfieldElements, "Settings textfields not found");

		// Change checkboxes for email
		falseCheckbox = new By[2];
		falseCheckbox[0] = By.id("mailTLS");
		falseCheckbox[1] = By.id("mailSMTPAuth");
		basics.useButton(falseCheckbox[0], "Mail TLS checkbox not usable.");
		basics.useButton(falseCheckbox[1], "Mail SMTP checkbox not usable.");
		// Click second time to show mail user and password
		basics.useButton(falseCheckbox[1], "Mail SMTP checkbox not usable.");

		// Removing a template
		String name = SeleniumPropUtils.getProperty("testRegex.name");
		String description = SeleniumPropUtils.getProperty("testRegex.description");
		String regex = SeleniumPropUtils.getProperty("testRegex.regex");

		List<WebElement> templates = getPseudonymRegexTemplates();
		Iterator<WebElement> iterator = templates.iterator();
		WebElement currentTemplate = null;

		boolean found = false;
		while (iterator.hasNext() && found == false) {
			currentTemplate = iterator.next();

			String failMessage = "No template name found.";
			WebElement currentName = basics.findWebElementWithin(currentTemplate, By.cssSelector("div[id='templateNameDiv']>input"), failMessage);
			failMessage = "No template description found.";
			WebElement currentDescription = basics.findWebElementWithin(currentTemplate, By.cssSelector("div[id='templateDescriptionDiv']>textarea"), failMessage);
			failMessage = "No template regex found.";
			WebElement currentRegex = basics.findWebElementWithin(currentTemplate, By.cssSelector("div[id='templateRegexDiv']>input"), failMessage);

			if (currentName.getAttribute("value").equals(name) && currentDescription.getText().equals(description) &&
					currentRegex.getAttribute("value").equals(regex)) {
				found = true;
			}
		}

		WebElement removeButton = basics.findWebElementWithin(currentTemplate, By.className("removePseudonymRegex"), "No remove template button found.");
		basics.useButton(removeButton, "Remove button not usable.");

		// Adding a new template
		basics.useButton(By.id("addPseudonymRegexButton"), "Add regex button not usable.");

		// Selecting wrong language
		Select languageSelection = new Select(basics.findWebElement(By.id("defaultLanguage"), "No selection found."));
		currentLanguageValue = languageSelection.getFirstSelectedOption().getAttribute("value");

		List<WebElement> languages = languageSelection.getOptions();
		Iterator<WebElement> iteratorLanguages = languages.iterator();

		String newLanguageValue = "";
		WebElement newLanguage = null;

		while (iteratorLanguages.hasNext() && newLanguageValue.isBlank()) {
			newLanguage = iteratorLanguages.next();
			String currentValue = newLanguage.getAttribute("value");

			if (!currentValue.equals(currentLanguageValue)) {
				newLanguageValue = currentValue;
			}
		}

		if (newLanguageValue.isBlank()) {
			fail("Other languages missing.");
		}

		languageSelection.selectByValue(newLanguageValue);
	}

	/**
	 * Makes sure all textfields with false input are not on the page any longer.
	 */
	public void verifyingInvisibleElements() {
		for (int i = 0; i < numberFalseTextfieldElements; i++) {
			basics.doNotFindWebElement(falseTextfieldElements[i].getSearchPath(), "The settings textfields should not be visible.");
		}
	}

	/**
	 * Checks if the values set up in 'prepareFalseInput()' were accidentally saved.
	 */
	public void checkForFalseInput() {
		// Finding all textfields again
		WebElement[] elements = basics.findWebElements(falseTextfieldElements, "Textfields with changed values not found after change.");

		// Getting old values
		String email = SeleniumPropUtils.getProperty("testEmail");
		String emailUsername = SeleniumPropUtils.getProperty("testEmail.name");
		String emailSystemHost = SeleniumPropUtils.getProperty("testEmail.host");
		String emailSystemPort = SeleniumPropUtils.getProperty("testEmail.port");
		String emailTel = SeleniumPropUtils.getProperty("testEmail.tel");
		String mainColor = "#" + SeleniumPropUtils.getProperty("colors.main");
		String highlightColor = "#" + SeleniumPropUtils.getProperty("colors.highlight");

		// Comparing current to old values
		assertEquals(emailSystemHost, elements[0].getAttribute("value"), "Email host has been changed despite cancelling.");
		assertEquals(emailSystemPort, elements[1].getAttribute("value"), "Email port has been changed despite cancelling.");
		assertEquals(emailUsername, elements[2].getAttribute("value"), "Email username has been changed despite cancelling.");
		assertEquals(email, elements[4].getAttribute("value"), "Email sender has been changed despite cancelling.");
		assertEquals(email, elements[5].getAttribute("value"), "Support email has been changed despite cancelling.");
		assertEquals(emailTel, elements[6].getAttribute("value"), "Support phone has been changed despite cancelling.");
		assertEquals(mainColor, elements[7].getAttribute("value"), "Main color has been changed despite cancelling.");
		assertEquals(highlightColor, elements[8].getAttribute("value"), "Highlight color has been changed despite cancelling.");

		// Checking checkboxes
		WebElement[] checkboxes = basics.findWebElements(falseCheckbox, "No checkboxes found.");
		assertTrue(checkboxes[0].isSelected(), "The TLS checkbox should not be selected.");
		assertTrue(checkboxes[1].isSelected(), "The SMTP checkbox should not be selected.");

		// Searching for accidentally deleted template
		// Searching for an accidentally created template
		String name = SeleniumPropUtils.getProperty("testRegex.name");
		String description = SeleniumPropUtils.getProperty("testRegex.description");
		String regex = SeleniumPropUtils.getProperty("testRegex.regex");

		List<WebElement> templates = getPseudonymRegexTemplates();
		final int numberTemplates = templates.size();

		boolean foundDeleted = false;
		boolean foundCreated = false;
		for (int i = 0; i < numberTemplates; i++) {
			final String idPrefix = "pseudonymRegexList" + i + ".";
			final String idPrefixInner = idPrefix + "pseudonymRegexDescriptionDTOList" + 1 + ".";

			basics.useButton(By.id("pseudonymRegex" + i + "-" + 1 + "Button"), "Unable to switch description language");

			String failMessage = "No template name found.";
			WebElement currentName = basics.findWebElement(By.id(idPrefixInner + "name"), failMessage);
			basics.waitForVisibility(currentName);

			failMessage = "No template description found.";
			WebElement currentDescription = basics.findWebElement(By.id(idPrefixInner + "description"), failMessage);

			failMessage = "No template regex found.";
			WebElement currentRegex = basics.findWebElement(By.id(idPrefix + "regex"), failMessage);

			String currentNameValue = currentName.getAttribute("value");
			if (currentNameValue.equals(name) && currentDescription.getText().equals(description) &&
					currentRegex.getAttribute("value").equals(regex)) {
				foundDeleted = true;
			} else if (currentNameValue.isEmpty()) {
				foundCreated = true;
			}
		}

		assertTrue(foundDeleted, "The deleted template was not found despite cancelling.");
		assertFalse(foundCreated, "There should be no empty template after cancelling.");

		// Searching for the selected language
		Select languageSelection = new Select(basics.findWebElement(By.id("defaultLanguage"), "No selection found."));
		String selectedLanguageValue = languageSelection.getFirstSelectedOption().getAttribute("value");

		assertEquals(currentLanguageValue, selectedLanguageValue, "The original default language is not selected despite cancelling.");
	}

	/**
	 * Tests whether the home button is usable and whether it canceled current changes.
	 */
	@Test
	public void homeButtonTest() {
		// Input, that shall not be saved
		prepareFalseInput();

		// Redirect with home button
		basics.redirect(By.id("cancelButton"), "Main page button not usable.");

		// Check if the page was actually left
		verifyingInvisibleElements();

		// Switching back
		By settingsButton = By.id("settings-button");
		basics.redirect(settingsButton, "Settings button not usable.");

		// Checking if something was accidentally saved
		checkForFalseInput();
	}

	/**
	 * Tests whether the current edit can be revoked by cancelling.
	 */
	@Test
	public void cancelTest() {
		// Inserting false input
		prepareFalseInput();

		// Cancelling
		By cancelButton = By.id("cancelButton");
		basics.redirect(cancelButton, "Cancel button not usable");

		// Check if the page was actually left
		verifyingInvisibleElements();

		// Switching back
		By settingsButton = By.id("settings-button");
		basics.redirect(settingsButton, "Settings button not usable.");

		// Checking if something was accidentally saved
		checkForFalseInput();
	}

	/**
	 * Tests whether a logout is possible and whether it cancels current changes.
	 */
	@Test
	public void logoutTest() {
		// Input, that shall not be saved
		prepareFalseInput();

		// Logout
		basics.logout();

		// Check if the page was actually left
		verifyingInvisibleElements();

		// Switching back
		basics.login(testUserName, testUserPass);

		By settingsButton = By.id("settings-button");
		basics.redirect(settingsButton, "Settings button not usable.");

		// Checking if something was accidentally saved
		checkForFalseInput();
	}

	private List<WebElement> getPseudonymRegexTemplates() {
		return basics.findSameTypeWebElementsWithin(
				basics.findWebElement(By.id("pseudonymTemplatesFieldset"),
				                      "Unable to find fieldset of pseudonym regex templates"),
				By.className("pseudonymRegexTemplateFieldset"), "No templates found.");
	}
}
