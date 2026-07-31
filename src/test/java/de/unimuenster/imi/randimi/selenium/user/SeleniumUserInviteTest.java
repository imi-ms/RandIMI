package de.unimuenster.imi.randimi.selenium.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumLanguageAutomation;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumTextfieldPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;

/**
 * Tests for the invite page of the user management.
 * Url: "/users/invite"
 * @author Anika Herbermann
 */
public class SeleniumUserInviteTest extends SeleniumBaseTest{

    private String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
    private String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

    private int numberFalseTextfieldElements;
    private SeleniumTextfieldPair[] falseTextfieldElements;
    private int numberFalseOtherElements;
    private By[] falseOtherElements;
    
    /**
     * Initialises the driver and basic functions and navigates to user invite page.
     */
    @BeforeEach
    public void setup() {
        // Setup on required page
        basics.login(testUserName, testUserPass);
        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");
        By userInviteButton = By.id("userInviteButton");
        basics.redirect(userInviteButton, "Cannot redirect to user invite page.");
    }

    /**
     * Tests, if an invitation can be send.
     */
    @Test
    public void userInviteTest() {
        // Values for new randomizer
        String email = SeleniumPropUtils.getProperty("testEmail");
        String firstname = "InviteFirstName";
        String lastname = "InviteLastName";
        String username = "InviteUserName";

        // Insert values into textfields
        int textfieldNumber = 4;
        SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[textfieldNumber];
        pairs[0] = new SeleniumTextfieldPair(By.id("firstName"), firstname);
        pairs[1] = new SeleniumTextfieldPair(By.id("lastName"), lastname);
        pairs[2] = new SeleniumTextfieldPair(By.id("eMail"), email);
        pairs[3] = new SeleniumTextfieldPair(By.id("username"), username);

        basics.findWebElementsAndWrite(pairs, "Could not use requested textfields.");

        // Send invitation
        basics.redirect(By.id("saveButton"),"Save button could not be used.");

        // Finding table entries
        List<WebElement> tableEntries = basics.findSameTypeWebElements(By.id("tableEntry"), "User missing in user overview.");

        // Finding the test user within the table
        Iterator<WebElement> iterator = tableEntries.iterator();
        WebElement currentElement = iterator.next();
        By searchPathUserName = By.id("tableEntryUserName");
        String failMessage = "User name could not be found within user overview.";
        String userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        String testRandomizerUserName = username.toUpperCase();

        while(!userName.equals(testRandomizerUserName) && iterator.hasNext()) {
            currentElement = iterator.next();
            userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        }

        // Finding all other table entries
        int numberTableEntries = 5;
        WebElement[] elements = new WebElement[numberTableEntries];
        elements[0] = basics.findWebElementWithin(currentElement, By.id("tableEntryStatus"), "No status for new user found.");
        elements[1] = basics.findWebElementWithin(currentElement, By.id("tableEntryFullName"), "No name for new user found.");
        elements[2] = basics.findWebElementWithin(currentElement, By.id("tableEntryEmail"), "No email for new user found.");
        elements[3] = basics.findWebElementWithin(currentElement, By.id("labelEditInvitation"), "No edit invitation button for new user found.");
        elements[4] = basics.findWebElementWithin(currentElement, By.id("labelResendInvitation"), "No resend invitation button for new user found.");

        // Checking, if text entries are correct
        String status = "false";
        assertEquals(status, elements[0].getText(), "The invited user should not be shown as activated.");
        assertEquals(firstname + " " + lastname, elements[1].getText(), "The full name of the invited user is incorrect.");
        assertEquals(email, elements[2].getText(), "The email of the invited user is incorrect.");
    }

    /**
     * Tests whether the user invite page shows all specific elements.
     */
    @Test
    public void visibilityTest() {
        // Preperation and search for HTML elements
        List<By> searchPaths = new ArrayList<>();
        searchPaths.add(By.id("headline"));
        searchPaths.add(By.tagName("legend"));
        searchPaths.add(By.id("firstName"));
        searchPaths.add(By.id("firstNameLabel"));
        searchPaths.add(By.id("lastName"));
        searchPaths.add(By.id("lastNameLabel"));
        searchPaths.add(By.id("eMail"));
        searchPaths.add(By.id("eMailLabel"));
        searchPaths.add(By.id("username"));
        searchPaths.add(By.id("usernameLabel"));
        searchPaths.add(By.id("userRoles_ROLE_API_USER"));
        searchPaths.add(By.id("userRoles_ROLE_STUDY_MANAGER"));
        searchPaths.add(By.id("userRoles_ROLE_USER_MANAGER"));
        searchPaths.add(By.id("userRoles_ROLE_ADMIN"));
        searchPaths.add(By.id("saveButton"));
        searchPaths.add(By.id("cancelButton"));

        WebElement[] elements = basics.findWebElements(searchPaths.toArray(new By[0]),
                                                       "Required change invitation elements not found.");

        // Check, if all elements can be seen
        String invisibleElements = "";
        for (int i = 0; i < searchPaths.size(); i++) {
            if(!elements[i].isDisplayed()) {
                if(invisibleElements.equals("")) {
                    invisibleElements = searchPaths.get(i).toString();
                } else {
                    invisibleElements += "', '" + searchPaths.get(i);
                }
            }
        }
        if (!invisibleElements.equals("")) {
            fail("Elements with search paths '" + invisibleElements + "' cannot be seen.");
        }
    }

    /**
     * Inserts values into textfields and checkboxes of the user invite page, that should not be saved on cancel.
     */
    public void prepareFalseInput() {
        // Text input, that shall not be saved
        String falseEmail = SeleniumPropUtils.getProperty("testEmail");
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

        basics.findWebElementsAndWrite(falseTextfieldElements, "User invite textfields not found");

        // Selecting false role
        numberFalseOtherElements = 3;
        falseOtherElements = new By[numberFalseOtherElements];
        falseOtherElements[0] = By.id("userRoles_ROLE_STUDY_MANAGER");
        falseOtherElements[1] = By.id("userRoles_ROLE_USER_MANAGER");
        falseOtherElements[2] = By.id("userRoles_ROLE_ADMIN");

        basics.useButton(falseOtherElements[0], "Checkbox for role study manager not usable.");
    }

    /**
     * Makes sure all texfields and checkboxes with false input are not on the page any longer.
     */
    public void verifyingInvisibleElements() {
        for (int i = 0; i < numberFalseTextfieldElements; i++) {
            basics.doNotFindWebElement(falseTextfieldElements[i].getSearchPath(), "The user invite textfields should not be visible.");
        }
        for (int i = 0; i < numberFalseOtherElements; i++) {
            basics.doNotFindWebElement(falseOtherElements[i], "The user invite checkboxes should not be visible.");
        }
    }

    /**
     * Checks if the values set up in 'prepareFalseInput()' were accidentally saved.
     */
    public void checkForFalseInput() {
        WebElement[] textfieldElements = basics.findWebElements(falseTextfieldElements, "Textfields can not be found.");

        WebElement studyManagerCheckbox = basics.findWebElement(falseOtherElements[0], "Study manager checkbox can not be found.");

        // Checking if textfields are empty and checkboxes not clicked
        assertEquals("", textfieldElements[0].getAttribute("value"), "First name has been changed despite cancelling.");
        assertEquals("", textfieldElements[1].getAttribute("value"), "Last name has been changed despite cancelling.");
        assertEquals("", textfieldElements[2].getAttribute("value"), "Email has been changed despite cancelling.");
        assertEquals("", textfieldElements[3].getAttribute("value"), "Suggested user name has been changed despite cancelling.");

        if (studyManagerCheckbox.isSelected()) {
            fail("The checkbox for the study manager should not be selected.");
        }

        // Checking if other checkboxes are not checked
        WebElement userManagerCheckbox = basics.findWebElement(falseOtherElements[1], "User manager checkbox can not be found.");
        WebElement adminCheckbox = basics.findWebElement(falseOtherElements[2], "Admin checkbox can not be found.");

        if (userManagerCheckbox.isSelected()) {
            fail("The checkbox for the user manager should not be selected.");
        }
        if (adminCheckbox.isSelected()) {
            fail("The checkbox for the admin should not be selected.");
        }
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
        By userInviteButton = By.id("userInviteButton");
        basics.redirect(userInviteButton, "User invite button not usable");

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
        By userInviteButton = By.id("userInviteButton");
        basics.redirect(userInviteButton, "User invite button not usable");

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
        By userInviteButton = By.id("userInviteButton");
        basics.redirect(userInviteButton, "User invite button not usable");

        // Checking if false input was accidentally saved
        checkForFalseInput();
    }

    /**
     * Tests the error messages of required textfields.
     */
    @Test
    public void missingInputTest() {
        // Values for new randomizer
        String email = SeleniumPropUtils.getProperty("testEmail");
        String firstname = "InviteFirstName";
        String lastname = "InviteLastName";
        String username = "InviteUserName";

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
