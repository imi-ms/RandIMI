package de.unimuenster.imi.randimi.selenium.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumLanguageAutomation;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumTextfieldPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;

/**
 * Tests for the activation page.
 * Url: "/users/activate"
 * @author Anika Herbermann
 */
public class SeleniumUserActivationTest extends SeleniumBaseTest{
    private String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
    private String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");
    
    /**
     * Initialises the driver and basic functions and navigates to activation page.
     */
    @BeforeEach
    public void setup() {
        // Login with different user as preperation for language test
        basics.login(testUserName, testUserPass);

        // Set up on external page
        String token = SeleniumPropUtils.getProperty("testuser.inactivatedUser.token");
        basics.loadPage("/users/activate?token=" + token);
    }

    /**
     * Tests, whether an invited user can activate himself.
     */
    @Test
    public void activateUserTest() {
        // New values
        String newPassword = "newPassword";
        String myUsername = "NowActivatedUser";
        String firstname = "NowActiveFirst";
        String lastname = "NowActiveLast";
        String eMail = "randimi@wwu.de";
        String display = lastname + ", " + firstname;

        // Insert values into textfields
        int numberElements = 6;
        SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[numberElements];
        pairs[0] = new SeleniumTextfieldPair(By.id("firstName"), firstname);
        pairs[1] = new SeleniumTextfieldPair(By.id("lastName"), lastname);
        pairs[2] = new SeleniumTextfieldPair(By.id("eMail"), eMail);
        pairs[3] = new SeleniumTextfieldPair(By.id("username"), myUsername);
        pairs[4] = new SeleniumTextfieldPair(By.id("password"), newPassword);
        pairs[5] = new SeleniumTextfieldPair(By.id("repeatPassword"), newPassword);

        basics.findWebElementsAndWrite(pairs, "Textfields not usable.");

        basics.redirect(By.id("saveButton"), "New user could not be saved.");

        // Using the activated user
        basics.loadMainPage();
        basics.logout();
        basics.login(myUsername, newPassword);

        // Check the name of the user
        WebElement displayname = basics.findWebElement(By.id("user"), "Displayed name not found.");
        assertEquals(display, displayname.getText(), "Displayed name is incorrect.");
    }

    /**
     * Tests whether the user activation page shows all specific elements.
     */
    @Test
    public void visibilityTest() {
        // Preperation and search for HTML elements
        int numberElements = 15;
        By[] searchPaths = new By[numberElements];
        
        searchPaths[0] = By.id("headline");
        searchPaths[1] = By.id("firstName");
        searchPaths[2] = By.id("firstNameLabel");
        searchPaths[3] = By.id("lastName");
        searchPaths[4] = By.id("lastNameLabel");
        searchPaths[5] = By.id("eMail");
        searchPaths[6] = By.id("eMailLabel");
        searchPaths[7] = By.id("username");
        searchPaths[8] = By.id("usernameLabel");
        searchPaths[9] = By.id("password");
        searchPaths[10] = By.id("passwordLabel");
        searchPaths[11] = By.id("repeatPassword");
        searchPaths[12] = By.id("repeatPasswordLabel");
        searchPaths[13] = By.id("saveButton");
        searchPaths[14] = By.id("cancelButton");

        WebElement[] elements = basics.findWebElements(searchPaths, "Required user activation elements not found.");

        // Check, if all elements can be seen
        String invisibleElements = "";
        for (int i = 0; i < numberElements; i++) {
            if(!elements[i].isDisplayed()) {
                if(invisibleElements.equals("")) {
                    invisibleElements = searchPaths[i].toString();
                } else {
                    invisibleElements += "', '" + searchPaths[i];
                }
            }
        }
        if (!invisibleElements.equals("")) {
            fail("Elements with search paths '" + invisibleElements + "' cannot be seen.");
        }

        // Supposedly suggested values
        String username = SeleniumPropUtils.getProperty("testuser.inactivatedUser.name");
        String email = SeleniumPropUtils.getProperty("testuser.inactivatedUser.email");
        String firstname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.firstname");
        String lastname = SeleniumPropUtils.getProperty("testuser.inactivatedUser.lastname");

        // Check for suggested values
        assertEquals(username.toUpperCase(), elements[7].getAttribute("value"), "The suggested username was not shown.");
        assertEquals(email, elements[5].getAttribute("value"), "The suggested email was not shown.");
        assertEquals(firstname, elements[1].getAttribute("value"), "The suggested first name was not shown.");
        assertEquals(lastname, elements[3].getAttribute("value"), "The suggested last name was not shown.");

        // Required for afterEach
        basics.loadMainPage();
    }

    /**
     * Tests all error messages of textfields, when they are left empty.
     */
    @Test
    public void missingInputTest() {
        // Some values
        String password = "somePassword";
        String myUsername = "SomeActivatedUser";
        String firstname = "SomeActiveFirst";
        String lastname = "SomeActiveLast";
        String eMail = "randimi@wwu.de";

        // Combining values with textfields
        int numberElements = 6;
        SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[numberElements];
        pairs[0] = new SeleniumTextfieldPair(By.id("firstName"), firstname);
        pairs[1] = new SeleniumTextfieldPair(By.id("lastName"), lastname);
        pairs[2] = new SeleniumTextfieldPair(By.id("eMail"), eMail);
        pairs[3] = new SeleniumTextfieldPair(By.id("username"), myUsername);
        pairs[4] = new SeleniumTextfieldPair(By.id("password"), password);
        pairs[5] = new SeleniumTextfieldPair(By.id("repeatPassword"), password);

        // Starting language and error test
        SeleniumLanguageAutomation auto = new SeleniumLanguageAutomation(basics);
        auto.checkErrorMessages("error", pairs, By.id("saveButton"));

        // Required for afterEach
        basics.loadMainPage();
    }

    /**
     * Tests if cancelling aborts the activation.
     */
    @Test
    public void cancelTest() {
        // False values
        String password = "falsePassword";
        String myUsername = "FalseActivatedUser";
        String firstname = "FalseActiveFirst";
        String lastname = "FalseActiveLast";
        String eMail = "randimi@wwu.de";

        // Insert values into textfields
        int numberElements = 6;
        SeleniumTextfieldPair[] pairs = new SeleniumTextfieldPair[numberElements];
        pairs[0] = new SeleniumTextfieldPair(By.id("firstName"), firstname);
        pairs[1] = new SeleniumTextfieldPair(By.id("lastName"), lastname);
        pairs[2] = new SeleniumTextfieldPair(By.id("eMail"), eMail);
        pairs[3] = new SeleniumTextfieldPair(By.id("username"), myUsername);
        pairs[4] = new SeleniumTextfieldPair(By.id("password"), password);
        pairs[5] = new SeleniumTextfieldPair(By.id("repeatPassword"), password);

        basics.findWebElementsAndWrite(pairs, "Textfields not usable.");

        // Press cancel button and try to login
        basics.redirect(By.id("cancelButton"), "Cancel button not usable.");

        // Set up on login page
        basics.loadMainPage();
        basics.logout();

        String failMessage = "Login fiels not usable.";
        SeleniumTextfieldPair[] searchPaths = new SeleniumTextfieldPair[2];
        searchPaths[0] = new SeleniumTextfieldPair(By.id("user"), myUsername);
        searchPaths[1] = new SeleniumTextfieldPair(By.id("pass"), password);
        basics.findWebElementsAndWrite(searchPaths, failMessage);

        basics.redirect(By.id("submit"), "Login button not usable.");

        // Checking if the expected error appeared
        basics.findWebElement(By.id("errorDiv"), "Error should have been shown");

        // Required for afterEach
        super.setLogoutWanted(false);
    }
}
