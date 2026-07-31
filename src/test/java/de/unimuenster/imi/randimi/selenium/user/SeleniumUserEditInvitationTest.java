package de.unimuenster.imi.randimi.selenium.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.*;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumLanguageAutomation;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumTextfieldPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;

/**
 * Tests for the change invite page of the user management.
 * Url: "/users/invite?id="
 * @author Anika Herbermann
 */
public class SeleniumUserEditInvitationTest extends SeleniumBaseTest {

    private String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
    private String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

    private int numberFalseTextfieldElements;
    private SeleniumTextfieldPair[] falseTextfieldElements;
    private int numberFalseOtherElements;
    private By[] falseOtherElements;
    
    /**
     * Initialises the driver and basic functions and navigates to edit invitation page.
     */
    @BeforeEach
    public void setup() {
        // Setup on required page
        basics.login(testUserName, testUserPass);
        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");

        useEditInvitationButton();
    }

    /**
     * Searches for the inactivated user within the table and clicks on its change invite button.
     */
    public void useEditInvitationButton() {
        useEditInvitationButton(SeleniumPropUtils.getProperty("testuser.inactivatedUser.name"));
    }

    /**
     * Searches for the user with the given username within the table and clicks on its change invite button.
     * @param inactivatedUserUsername Username of the user.
     */
    public void useEditInvitationButton(String inactivatedUserUsername) {
        // TODO Temporary fix
        WebElement nextPageButton = basics.findWebElement(By.cssSelector("div.dt-paging .dt-paging-button.next"), "Can't find button!");
        basics.useButton(nextPageButton, "Can't go to next page!");

        inactivatedUserUsername = inactivatedUserUsername.toUpperCase();

        // Finding table entries
        List<WebElement> tableEntries = basics.findSameTypeWebElements(By.id("tableEntry"), "User missing in user overview.");

        // Finding the test inactivated user within the table
        Iterator<WebElement> iterator = tableEntries.iterator();
        WebElement currentElement = iterator.next();
        By searchPathUserName = By.id("tableEntryUserName");
        String failMessage = "User name could not be found within user overview.";
        String userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();

        while(!userName.equals(inactivatedUserUsername) && iterator.hasNext()) {
            currentElement = iterator.next();
            userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        }

        // Picking change invitation for the test inactivated user
        if (userName.equals(inactivatedUserUsername)) {
            failMessage = "Change invitation button not found.";
            WebElement button = basics.findWebElementWithin(currentElement, By.id("buttonEditInvitation"), failMessage);
            basics.useButton(button, "Change invitation button not usable.");
        } else {
            fail("Inactive user could not be found.");
        }
    }

    /**
     * Tests, if changes to an invitation will be stored successfully.
     */
    @Test
    public void editInvitationTest() {
        // Values for a change to a new study manager
        String email = "rand1m1@wwu.de";
        String firstname = "InviteFirstName";
        String lastname = "InviteLastName";
        String username = "InviteUserName";

        // Checkboxes
        By[] checkboxPaths = new By[3];
        checkboxPaths[0] = By.id("userRoles_ROLE_ADMIN");
        checkboxPaths[1] = By.id("userRoles_ROLE_STUDY_MANAGER");
        checkboxPaths[2] = By.id("userRoles_ROLE_USER_MANAGER");

        // Entering the values
        int numberTextfieldElements = 4;
        SeleniumTextfieldPair[] textfieldElements = new SeleniumTextfieldPair[numberTextfieldElements];
        textfieldElements[0] = new SeleniumTextfieldPair(By.id("firstName"), firstname);
        textfieldElements[1] = new SeleniumTextfieldPair(By.id("lastName"), lastname);
        textfieldElements[2] = new SeleniumTextfieldPair(By.id("eMail"), email);
        textfieldElements[3] = new SeleniumTextfieldPair(By.id("username"), username);

        basics.findWebElementsAndWrite(textfieldElements, "Change invitation textfields not found.");

        basics.useButton(checkboxPaths[0], "Admin checkbox not clickable.");
        basics.useButton(checkboxPaths[1], "Study manager checkbox not clickable.");

        // Saving values
        basics.redirect(By.id("saveButton"), "Save button not usable.");

        // Switching back
        useEditInvitationButton(username);

        // Checking new values
        WebElement[] textfields = basics.findWebElements(textfieldElements, "No textfields found.");
        WebElement[] checkboxes = basics.findWebElements(checkboxPaths, "No checkboxes found.");

        assertEquals(firstname, textfields[0].getAttribute("value"), "First name was not changed.");
        assertEquals(lastname, textfields[1].getAttribute("value"), "Last name was not changed.");
        assertEquals(email, textfields[2].getAttribute("value"), "Email was not changed.");
        assertEquals(username.toUpperCase(), textfields[3].getAttribute("value"), "User name was not changed.");

        assertFalse(checkboxes[0].isSelected(), "Admin checkbox should not be selected.");
        assertTrue(checkboxes[1].isSelected(), "Study manager checkbox should be selected.");
        assertFalse(checkboxes[2].isSelected(), "User manager checkbox should not be selected.");
    }

    /**
     * Tests whether the change invitation page shows all specific elements.
     */
    @Test
    public void visibilityTest() {
        // Preparation and search for HTML elements
        final Map<String, By> searchPaths = basics.buildById(
                "headline",
                "editUserFieldsetLegend",
                "firstName",
                "firstNameLabel",
                "lastName",
                "lastNameLabel",
                "eMail",
                "eMailLabel",
                "username",
                "usernameLabel",
                "userRoles_ROLE_API_USER",
                "userRoles_ROLE_STUDY_MANAGER",
                "userRoles_ROLE_USER_MANAGER",
                "userRoles_ROLE_ADMIN",
                "saveButton",
                "cancelButton"
        );

        final var elements = basics.findWebElements(searchPaths, "Required change invitation elements not found.");
        basics.checkVisibility(elements);

        // Pre defined values
        String username = SeleniumPropUtils.getProperty("testuser.inactivatedUser.name");
        String email = SeleniumPropUtils.getProperty("testuser.inactivatedUser.email");
        String firstname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.firstname");
        String lastname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.lastname");

        // Check for suggested values
        assertEquals(firstname, elements.get("firstName").getAttribute("value"),
                     "The suggested first name was not shown.");
        assertEquals(lastname, elements.get("lastName").getAttribute("value"),
                     "The suggested last name was not shown.");
        assertEquals(email, elements.get("eMail").getAttribute("value"), "The suggested email was not shown.");
        assertEquals(username.toUpperCase(), elements.get("username").getAttribute("value"),
                     "The suggested username was not shown.");

        assertTrue(elements.get("userRoles_ROLE_ADMIN").isSelected(), "The admin checkbox should be selected");
        assertFalse(elements.get("userRoles_ROLE_USER_MANAGER").isSelected(),
                    "The user manager checkbox should not be selected");
        assertFalse(elements.get("userRoles_ROLE_USER_MANAGER").isSelected(),
                    "The study manager checkbox should not be selected");
        assertFalse(elements.get("userRoles_ROLE_API_USER").isSelected(),
                    "The api user checkbox should not be selected");
    }

    /**
     * Inserts values into textfields and checkboxes of the change invitation page, that should not be saved on cancel.
     */
    public void prepareFalseInput() {
        // Text input, that shall not be saved
        String falseEmail = "rand1m1@wwu.de";
        String falseFirstName = "FalseFirstName";
        String falseLastName = "FalseLastName";
        String falseUserName = "FalseUsername";

        // Inserting text input
        numberFalseTextfieldElements = 4;
        falseTextfieldElements = new SeleniumTextfieldPair[numberFalseTextfieldElements];
        falseTextfieldElements[0] = new SeleniumTextfieldPair(By.id("firstName"), falseFirstName);
        falseTextfieldElements[1] = new SeleniumTextfieldPair(By.id("lastName"), falseLastName);
        falseTextfieldElements[2] = new SeleniumTextfieldPair(By.id("eMail"), falseEmail);
        falseTextfieldElements[3] = new SeleniumTextfieldPair(By.id("username"), falseUserName);

        basics.findWebElementsAndWrite(falseTextfieldElements, "Change invitation textfields not found.");

        // Selecting false role
        numberFalseOtherElements = 3;
        falseOtherElements = new By[numberFalseOtherElements];
        falseOtherElements[0] = By.id("userRoles_ROLE_STUDY_MANAGER");
        falseOtherElements[1] = By.id("userRoles_ROLE_USER_MANAGER");
        falseOtherElements[2] = By.id("userRoles_ROLE_ADMIN");

        basics.useButton(falseOtherElements[0], "Checkbox for role study manager not usable.");
        basics.useButton(falseOtherElements[2], "Checkbox for admin not usable.");
    }

    /**
     * Makes sure all textfields and checkboxes with false input are not on the page any longer.
     */
    public void verifyingInvisibleElements() {
        for (int i = 0; i < numberFalseTextfieldElements; i++) {
            basics.doNotFindWebElement(falseTextfieldElements[i].getSearchPath(), "The change invitation textfields should not be visible.");
        }
        for (int i = 0; i < numberFalseOtherElements; i++) {
            basics.doNotFindWebElement(falseOtherElements[i], "The change invitation checkboxes should not be visible.");
        }
    }

    /**
     * Checks if the values set up in 'prepareFalseInput()' were accidentally saved.
     */
    public void checkForFalseInput() {
        // Searching for all input elements
        WebElement[] textfieldElements = basics.findWebElements(falseTextfieldElements, "Textfields can not be found.");
        WebElement[] otherElements = basics.findWebElements(falseOtherElements, "Checkboxes can not be found.");

        // Pre defined values
        String username = SeleniumPropUtils.getProperty("testuser.inactivatedUser.name");
        String email = SeleniumPropUtils.getProperty("testuser.inactivatedUser.email");
        String firstname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.firstname");
        String lastname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.lastname");

        // Checking if textfields have the predefined values
        assertEquals(firstname, textfieldElements[0].getAttribute("value"), "First name has been changed despite cancelling.");
        assertEquals(lastname, textfieldElements[1].getAttribute("value"), "Last name has been changed despite cancelling.");
        assertEquals(email, textfieldElements[2].getAttribute("value"), "Email has been changed despite cancelling.");
        assertEquals(username.toUpperCase(), textfieldElements[3].getAttribute("value"), "Suggested user name has been changed despite cancelling.");

        // Checking which checkboxes are clicked
        assertFalse(otherElements[0].isSelected(), "The checkbox for the study manager should not be selected.");
        assertFalse(otherElements[1].isSelected(), "The checkbox for the user manager should not be selected.");
        assertTrue(otherElements[2].isSelected(), "The checkbox for the admin should be selected.");
    }

    /**
     * Tests whether the home button is usable and whether it canceled current changes.
     */
    @Test
    public void homeButtonTest() {
        // Inserting false input
        prepareFalseInput();

        // Cancelling
        By cancelButton = By.id("main-page-button");
        basics.redirect(cancelButton, "Main page button not usable");

        // Check if the page was actually left
        verifyingInvisibleElements();

        // Switching back
        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");
        
        useEditInvitationButton();

        // Checking if false input was accidentally saved
        checkForFalseInput();
    }

    /**
     * Tests whether a logout is possible and whether it cancels current changes.
     */
    @Test
    public void logoutTest() {
        // Inserting false input
        prepareFalseInput();

        // Cancelling
        basics.logout();

        // Check if the page was actually left
        verifyingInvisibleElements();

        // Switching back
        basics.login(testUserName, testUserPass);
        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");
        
        useEditInvitationButton();

        // Checking if false input was accidentally saved
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
        useEditInvitationButton();

        // Checking if false input was accidentally saved
        checkForFalseInput();
    }

    /**
     * Tests the error messages of required textfields.
     */
    @Test
    public void missingInputTest() {
        // Pre defined values
        String username = SeleniumPropUtils.getProperty("testuser.inactivatedUser.name");
        String email = SeleniumPropUtils.getProperty("testuser.inactivatedUser.email");
        String firstname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.firstname");
        String lastname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.lastname");

        // Insert values into textfields
        int textfieldNumber = 4;
        SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[textfieldNumber];
        pairs[0] = new SeleniumTextfieldPair(By.id("firstName"), firstname);
        pairs[1] = new SeleniumTextfieldPair(By.id("lastName"), lastname);
        pairs[2] = new SeleniumTextfieldPair(By.id("eMail"), email);
        pairs[3] = new SeleniumTextfieldPair(By.id("username"), username);

        SeleniumLanguageAutomation auto = new SeleniumLanguageAutomation(basics);

        auto.checkErrorMessages("error", pairs, By.id("saveButton"));
    }
}
