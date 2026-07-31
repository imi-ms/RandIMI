package de.unimuenster.imi.randimi.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumLanguageAutomation;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumTextfieldPair;
import org.junit.jupiter.api.BeforeEach;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Test for the profile settings page.
 * Url: "/users/edit"
 * @author Anika Herbermann
 */
public class SeleniumProfileTest extends SeleniumBaseTest{
    private String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
    private String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

    private int numberFalseTextfieldElements;
    private SeleniumTextfieldPair[] falseTextfieldElements;

    /**
     * Initialises the driver and basic functions and navigates to profile page.
     */
    @BeforeEach
    public void setup() {
        // Setup on required page
        basics.login(testUserName, testUserPass);
        By profileButton = By.id("user-edit-button");
        basics.redirect(profileButton, "Cannot redirect to profile page.");
    }

    /**
     * Tests whether the profile page shows all specific elements and checks existing values.
     */
    @Test
    public void visibilityTest() {
        // Preperation and search for HTML elements
        int numberVisibleElements = 12;
        int numberInvisibleElements = 6;
        By[] visibleSearchPaths = new By[numberVisibleElements];
        By[] invisibleSearchPaths = new By[numberInvisibleElements];

        visibleSearchPaths[0] = By.id("headline");
        visibleSearchPaths[1] = By.tagName("legend");
        visibleSearchPaths[2] = By.id("firstName");
        visibleSearchPaths[3] = By.id("firstNameLabel");
        visibleSearchPaths[4] = By.id("lastName");
        visibleSearchPaths[5] = By.id("lastNameLabel");
        visibleSearchPaths[6] = By.id("mailAddress");
        visibleSearchPaths[7] = By.id("mailAddressLabel");
        visibleSearchPaths[8] = By.id("updatePasswordLabel");
        visibleSearchPaths[9] = By.id("updatePassword");
        visibleSearchPaths[10] = By.id("saveButton");
        visibleSearchPaths[11] = By.id("cancelButton");

        invisibleSearchPaths[0] = By.id("oldPassword");
        invisibleSearchPaths[1] = By.id("oldPasswordLabel");
        invisibleSearchPaths[2] = By.id("newPassword");
        invisibleSearchPaths[3] = By.id("newPasswordLabel");
        invisibleSearchPaths[4] = By.id("repeatPassword");
        invisibleSearchPaths[5] = By.id("repeatPasswordLabel");

        WebElement[] visibleElements = basics.findWebElements(visibleSearchPaths, "Required visible profile elements not found.");

        // Check, if all elements, that should be visible, can be seen
        String notVisibleElements = "";
        for (int i = 0; i < numberVisibleElements; i++) {
            if(!visibleElements[i].isDisplayed()) {
                if(notVisibleElements.equals("")) {
                    notVisibleElements = visibleSearchPaths[i].toString();
                } else {
                    notVisibleElements += "', '" + visibleSearchPaths[i];
                }
            }
        }
        if (!notVisibleElements.equals("")) {
            fail("Elements with search paths '" + notVisibleElements + "' cannot be seen.");
        }

        // Check if the inputs to change the password are invisible
        WebElement[] invisibleElements = basics.findWebElements(invisibleSearchPaths, "Required invisible profile elements not found.");
        String notInvisibleElements = "";
        for (int i = 0; i < numberInvisibleElements; i++) {
            if(invisibleElements[i].isDisplayed()) {
                if(notInvisibleElements.equals("")) {
                    notInvisibleElements = invisibleSearchPaths[i].toString();
                } else {
                    notInvisibleElements += "', '" + invisibleSearchPaths[i];
                }
            }
        }
        if (!notInvisibleElements.equals("")) {
            fail("Elements with search paths '" + notInvisibleElements + "' can be seen.");
        }

        basics.useButton(visibleElements[9], "Checkbox for updating the password could not be used!");

        // Check if the elements are visible after toggling the checkbox
        invisibleElements = basics.findWebElements(invisibleSearchPaths, "Required invisible profile elements not found.");
        notInvisibleElements = "";
        for (int i = 0; i < numberInvisibleElements; i++) {
            if(!invisibleElements[i].isDisplayed()) {
                if(notInvisibleElements.equals("")) {
                    notInvisibleElements = invisibleSearchPaths[i].toString();
                } else {
                    notInvisibleElements += "', '" + invisibleSearchPaths[i];
                }
            }
        }
        if (!notInvisibleElements.equals("")) {
            fail("Elements with search paths '" + notInvisibleElements + "' cannot be seen after toggling.");
        }

        // Checking old values
        String oldFirstName = SeleniumPropUtils.getProperty("testuser.admin.firstname");
        String oldLastName = SeleniumPropUtils.getProperty("testuser.admin.lastname");
        assertEquals(oldFirstName, visibleElements[2].getAttribute("value"), "Old profile first name is not shown correctly.");
        assertEquals(oldLastName, visibleElements[4].getAttribute("value"), "Old profile last name is not shown correctly.");

        String oldEmail = SeleniumPropUtils.getProperty("testuser.admin.email");
        assertEquals(oldEmail, visibleElements[6].getAttribute("value"), "Old email address is not shown correctly.");
    }

    /**
     * Tests whether a new name can bes set for the current user.
     */
    @Test
    public void nameChangeTest() {
        // New values to be checked
        String newFirstName = "NewFirstName";
        String newLastName = "NewLastName";
        String newDisplayName = newLastName + ", " + newFirstName;

        // Searching for textfields
        By[] nameSearchPaths = {By.id("firstName"), By.id("lastName")};
        WebElement[] nameTextfields = basics.findWebElements(nameSearchPaths, "Name textfields not found.");

        // Inserting new values
        basics.writeInTextfield(nameTextfields[0],newFirstName);
        basics.writeInTextfield(nameTextfields[1],newLastName);

        // Submitting change
        By saveButton = By.id("saveButton");
        basics.redirect(saveButton, "Cannot save the profile changes.");

        // Search for new values
        WebElement nameDisplay = basics.findWebElement(By.id("user"), "Username display not found.");
        nameTextfields = basics.findWebElements(nameSearchPaths, "Name textfields after name change not found.");
        
        assertEquals(newDisplayName, nameDisplay.getText(), "Display name not shown correctly.");
        assertEquals(newFirstName, nameTextfields[0].getAttribute("value"), "New profile first name is not shown correctly.");
        assertEquals(newLastName, nameTextfields[1].getAttribute("value"), "New profile last name is not shown correctly.");
    }

    /**
     * Tests whether a new email can be set for the current user.
     */
    @Test
    public void emailChangeTest() {
        // New values to be checked
        String newEmail = "rand1m1@wwu.de";

        // Searching for textfields
        By emailSearchPath = By.id("mailAddress");
        WebElement emailTextfield = basics.findWebElement(emailSearchPath, "Email textfield can not be found.");

        // Inserting new values
        basics.writeInTextfield(emailTextfield, newEmail);

        // Submitting change
        By saveButton = By.id("saveButton");
        basics.redirect(saveButton, "Cannot save the profile changes.");

        // Search for new values
        emailTextfield = basics.findWebElement(emailSearchPath, "Email textfield after change not found.");
        
        assertEquals(newEmail, emailTextfield.getAttribute("value"), "New email is not shown correctly.");
    }

    /**
     * Tests whether a new password can be set for the current user.
     */
    @Test
    public void passwordChangeTest() {
        // New values to be checked
        String newPassword = "testPassword";

        // Toggle checkbox in order to change the password
        basics.useButton(By.id("updatePassword"), "Checkbox for updating the password could not be used!");

        // Searching for textfields and writing new password
        SeleniumTextfieldPair oldPasswordPair = new SeleniumTextfieldPair(By.id("oldPassword"), testUserPass);
        SeleniumTextfieldPair passwordPair = new SeleniumTextfieldPair(By.id("newPassword"), newPassword);
        SeleniumTextfieldPair repeatPasswordPair = new SeleniumTextfieldPair(By.id("repeatPassword"), newPassword);
        SeleniumTextfieldPair[] passwordPairs = {oldPasswordPair, passwordPair, repeatPasswordPair};
        basics.findWebElementsAndWrite(passwordPairs, "No usable password textfields.");

        // Submitting change
        By saveButton = By.id("saveButton");
        basics.redirect(saveButton, "Cannot save the profile changes.");

        // Logout
        basics.logout();

        // Try login with new password
        basics.login(testUserName, newPassword);
    }

    /**
     * Tests the error messages for required textfields.
     */
    @Test
    public void missingInputTest() {
        // Setting up input
        String oldFirstName = SeleniumPropUtils.getProperty("testuser.admin.firstname");
        String oldLastName = SeleniumPropUtils.getProperty("testuser.admin.lastname");
        String oldEmail = SeleniumPropUtils.getProperty("testuser.admin.email");

        // Preparing textfields
        int numberTextfields = 3;
        SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[numberTextfields];
        pairs[0] = new SeleniumTextfieldPair(By.id("firstName"), oldFirstName);
        pairs[1] = new SeleniumTextfieldPair(By.id("lastName"), oldLastName);
        pairs[2] = new SeleniumTextfieldPair(By.id("mailAddress"), oldEmail);

        // Starting test
        SeleniumLanguageAutomation auto = new SeleniumLanguageAutomation(basics);
        auto.checkErrorMessages("error", pairs, By.id("saveButton"));
    }

    /**
     * Inserts values into textfields of the profile page, that should not be saved on cancel.
     */
    public void prepareFalseInput() {
        // Input, that shall not be saved
        String falseFirstName = "FalseFirstName";
        String falseLastName = "FalseLastName";
        String falseEmail = "randimi@wwu.de";

        // Inserting input
        numberFalseTextfieldElements = 3;
        falseTextfieldElements = new SeleniumTextfieldPair[numberFalseTextfieldElements];
        falseTextfieldElements[0] = new SeleniumTextfieldPair(By.id("firstName"), falseFirstName);
        falseTextfieldElements[1] = new SeleniumTextfieldPair(By.id("lastName"), falseLastName);
        falseTextfieldElements[2] = new SeleniumTextfieldPair(By.id("mailAddress"), falseEmail);

        basics.findWebElementsAndWrite(falseTextfieldElements, "Profile textfields not found");
    }

    /**
     * Makes sure all textfields with false input are not on the page any longer.
     */
    public void verifyingInvisibleElements() {
        for (int i = 0; i < numberFalseTextfieldElements; i++) {
            basics.doNotFindWebElement(falseTextfieldElements[i].getSearchPath(), "The profile textfields should not be visible.");
        }
    }

    /**
     * Checks if the values set up in 'prepareFalseInput()' were accidentally saved.
     */
    public void checkForFalseInput() {
        WebElement firstNameElement = basics.findWebElement(falseTextfieldElements[0].getSearchPath(), "First name textfield can not be found.");
        WebElement lastNameElement = basics.findWebElement(falseTextfieldElements[1].getSearchPath(), "Last name textfield can not be found.");
        WebElement emailElement = basics.findWebElement(falseTextfieldElements[2].getSearchPath(), "Email textfield can not be found.");

        WebElement nameDisplay = basics.findWebElement(By.id("user"), "Username display not found.");

        // Checking old values
        String oldFirstName = SeleniumPropUtils.getProperty("testuser.admin.firstname");
        String oldLastName = SeleniumPropUtils.getProperty("testuser.admin.lastname");
        String oldDisplayName = SeleniumPropUtils.getProperty("testuser.admin.displayName");
        String oldEmail = SeleniumPropUtils.getProperty("testuser.admin.email");

        assertEquals(oldFirstName, firstNameElement.getAttribute("value"), "First name has been changed despite cancelling.");
        assertEquals(oldLastName, lastNameElement.getAttribute("value"), "Last name has been changed despite cancelling.");
        assertEquals(oldDisplayName, nameDisplay.getText(), "Display name has been changed despite cancelling.");
        assertEquals(oldEmail, emailElement.getAttribute("value"), "Email has been changed despite cancelling.");
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
        basics.redirect(cancelButton, "Cancelbutton not usable");

        // Check if the page was actually left
        verifyingInvisibleElements();

        // Switching back
        By userEditButton = By.id("user-edit-button");
        basics.redirect(userEditButton, "User edit button not usable.");

        // Checking if something was accidentally saved
        checkForFalseInput();

        // Checking if the original login data was not overwritten
        String oldUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
        String oldPassword = SeleniumPropUtils.getProperty("testuser.admin.pass");

        basics.logout();
        basics.login(oldUserName, oldPassword);
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

        // Switching back and checking whether the password was changed
        String oldUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
        String oldPassword = SeleniumPropUtils.getProperty("testuser.admin.pass");
        basics.login(oldUserName, oldPassword);

        By userEditButton = By.id("user-edit-button");
        basics.redirect(userEditButton, "User edit button not usable.");

        // Checking if something was accidentally saved
        checkForFalseInput();
    }

    /**
     * Tests whether the home button is usable and whether it canceled current changes.
     */
    @Test
    public void homeButtonTest() {
        // Input, that shall not be saved
        prepareFalseInput();

        // Redirect with home button
        basics.redirect(By.id("main-page-button"), "Main page button not usable.");

        // Check if the page was actually left
        verifyingInvisibleElements();

        // Switching back
        By userEditButton = By.id("user-edit-button");
        basics.redirect(userEditButton, "User edit button not usable.");

        // Checking if something was accidentally saved
        checkForFalseInput();

        // Checking if the original login data was not overwritten
        String oldUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
        String oldPassword = SeleniumPropUtils.getProperty("testuser.admin.pass");

        basics.logout();
        basics.login(oldUserName, oldPassword);
    }
}
