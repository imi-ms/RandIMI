package de.unimuenster.imi.randimi.selenium.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumTextfieldPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;

/**
 * Tests for the user management page.
 * Url: "/users"
 * @author Anika Herbermann
 */
public class SeleniumUserOverviewTest extends SeleniumBaseTest{

    private String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
    private String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");
    
    /**
     * Initialises the driver and basic functions and navigates to user overview page.
     */
    @BeforeEach
    public void setup() {
        // Setup on required page
        basics.login(testUserName, testUserPass);
        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");
    }

    /**
     * Tests whether a disabled user can still log in to the web service.
     */
    @Test
    public void userDisableTest() {
        // Finding all displayed users
        List<WebElement> tableEntries = basics.findSameTypeWebElements(By.id("tableEntry"), "User missing in user overview.");

        // Locating the user manager, so that it can be disabled.
        Iterator<WebElement> iterator = tableEntries.iterator();
        WebElement currentElement = iterator.next();
        By searchPathUserName = By.id("tableEntryUserName");
        String failMessage = "User name could not be found within user overview.";
        String userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        String userManagerUserName = SeleniumPropUtils.getProperty("testuser.userManager.name").toUpperCase();

        while(!userName.equals(userManagerUserName) && iterator.hasNext()) {
            currentElement = iterator.next();
            userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        }

        // Disabling user manager
        if (userName.equals(userManagerUserName)) {
            failMessage = "Disable button not found.";
            try {
                basics.findWebElementWithin(currentElement, By.id("buttonDisableUser"), failMessage).sendKeys(Keys.ENTER);
            } catch (Exception e) {
                fail("Disable button not usable.");
            }
        }

        // Trying to switch to disabled user
        basics.logout();

        failMessage = "No login fields found.";
        SeleniumTextfieldPair[] searchPaths = new SeleniumTextfieldPair[2];
        searchPaths[0] = new SeleniumTextfieldPair(By.id("user"), userManagerUserName);
        searchPaths[1] = new SeleniumTextfieldPair(By.id("pass"), SeleniumPropUtils.getProperty("testuser.userManager.pass"));
        basics.findWebElementsAndWrite(searchPaths, failMessage);

        basics.redirect(By.id("submit"), "Login button not usable.");

        // Checking if an error appeared
        failMessage = "Error should have been shown";
        basics.findWebElement(By.id("errorDiv"), failMessage);

        // Required for afterEach
        super.setLogoutWanted(false);
    }

    @Test
    public void userEnableTest() {
        // Checking, if the given user is disabled
        basics.logout();

        String failMessage = "No login fields found.";
        SeleniumTextfieldPair[] searchPaths = new SeleniumTextfieldPair[2];
        searchPaths[0] = new SeleniumTextfieldPair(By.id("user"), SeleniumPropUtils.getProperty("testuser.disabledUser.name"));
        searchPaths[1] = new SeleniumTextfieldPair(By.id("pass"), SeleniumPropUtils.getProperty("testuser.disabledUser.pass"));
        basics.findWebElementsAndWrite(searchPaths, failMessage);

        basics.redirect(By.id("submit"), "Login button not usable.");

        // Checking if the expected error appeared
        basics.findWebElement(By.id("errorDiv"), "Error should have been shown");

        // Login with different account
        basics.loadMainPage();
        basics.login(testUserName, testUserPass);

        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");
        
        // Finding all displayed users
        List<WebElement> tableEntries = basics.findSameTypeWebElements(By.id("tableEntry"), "User missing in user overview.");

        // Locating the disabled user, so that it can be enabled.
        Iterator<WebElement> iterator = tableEntries.iterator();
        WebElement currentElement = iterator.next();
        By searchPathUserName = By.id("tableEntryUserName");
        failMessage = "User name could not be found within user overview.";
        String userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        String userManagerUserName = SeleniumPropUtils.getProperty("testuser.disabledUser.name").toUpperCase();

        while(!userName.equals(userManagerUserName) && iterator.hasNext()) {
            currentElement = iterator.next();
            userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        }

        // Enabling the user
        if (userName.equals(userManagerUserName)) {
            failMessage = "Enable button not found.";
            try {
                basics.findWebElementWithin(currentElement, By.id("buttonEnableUser"), failMessage).sendKeys(Keys.ENTER);
            } catch (Exception e) {
                fail("Enable button not usable.");
            }
        }

        // Testing new login
        basics.logout();
        basics.login(searchPaths[0].getValue(), searchPaths[1].getValue());
    }

    @Test
    public void resendInvitationTest() {
        // Finding table entries
        List<WebElement> tableEntries = basics.findSameTypeWebElements(By.id("tableEntry"), "User missing in user overview.");

        // Finding the test inactivated user within the table
        Iterator<WebElement> iterator = tableEntries.iterator();
        WebElement currentElement = iterator.next();
        By searchPathUserName = By.id("tableEntryUserName");
        String failMessage = "User name could not be found within user overview.";
        String userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        String inactivatedUserUsername = "INACTIVE_TEST_USER"; // SeleniumPropUtils.getProperty("testuser.inactivatedUser.name").toUpperCase();

        while(!userName.equals(inactivatedUserUsername) && iterator.hasNext()) {
            currentElement = iterator.next();
            userName = basics.findWebElementWithin(currentElement, searchPathUserName, failMessage).getText();
        }

        // Picking resend invitation for the test inactivated user
        if (userName.equals(inactivatedUserUsername)) {
            failMessage = "Resend invitation button not found.";
            WebElement button = basics.findWebElementWithin(currentElement, By.id("buttonViewInvitation"), failMessage);
            basics.useButton(button, "Resend invitation button not usable.");
        }

        // TODO rewrite
    }

    /**
     * Tests whether the user management overview shows all specific elements.
     */
    @Test
    public void visibilityTest() {
        // Preparation and search for HTML elements
        final Map<String, By> searchPaths = basics.buildById(
                "userMonitorHeadline",
                "userInviteButton",
                "returnButton",
                "tableHeadUserName",
                "tableHeadStatus",
                "tableHeadFullName",
                "tableHeadEmail",
                "tableHeadActions"
        );

        final Map<String, WebElement> elements = basics.findWebElements(searchPaths,
                                                                        "Required user management overview elements not found.");
        basics.checkVisibility(elements);

        // Get table entries and check if all expected users are found
        List<WebElement> tableEntries = basics.findSameTypeWebElements(By.id("tableEntry"), "User missing in user overview.");

        // Admin, test users and Selenium users
        int numberUsers = Integer.parseInt(SeleniumPropUtils.getProperty("number.users"));
        assertEquals(numberUsers, tableEntries.size(), "The table should have exactly 10 rows for 10 users.");

        Iterator<WebElement> iterator = tableEntries.iterator();

        // Values of three specific user
        int numberExampleUsers = 3;
        int numberTextValues = 4;
        String[][] userTextValues = new String[numberExampleUsers][numberTextValues];

        String[] testAdmin = {
                SeleniumPropUtils.getProperty("testuser.admin.name"),
                SeleniumPropUtils.getProperty("testuser.admin.activated"),
                SeleniumPropUtils.getProperty("testuser.admin.firstname") + " " + SeleniumPropUtils.getProperty("testuser.admin.lastname"),
                SeleniumPropUtils.getProperty("testuser.admin.email")
        };
        String[] disabledUser = {
                SeleniumPropUtils.getProperty("testuser.disabledUser.name"),
                SeleniumPropUtils.getProperty("testuser.disabledUser.activated"),
                SeleniumPropUtils.getProperty("testuser.disabledUser.fullname"),
                SeleniumPropUtils.getProperty("testuser.disabledUser.email")
        };
        String[] inactivatedUser = {
                SeleniumPropUtils.getProperty("testuser.inactivatedUser.name"),
                SeleniumPropUtils.getProperty("testuser.inactivatedUser.activated"),
                SeleniumPropUtils.getProperty("testuser.inactivatedUser.firstname") + " " + SeleniumPropUtils.getProperty("testuser.inactivatedUser.lastname"),
                SeleniumPropUtils.getProperty("testuser.inactivatedUser.email")
        };

        userTextValues[0] = testAdmin;
        userTextValues[1] = disabledUser;
        userTextValues[2] = inactivatedUser;

        int numberButtons = 2;
        By[][] userButtons = new By[numberExampleUsers][numberButtons];

        By[] testAdminButtons = {By.id("buttonDisableUser"), By.id("buttonEditUserRoles")};
        By[] disabledUserButtons = {By.id("buttonEnableUser"), By.id("buttonEditUserRoles")};
        By[] inactivatedUserButtons = {By.id("buttonEditInvitation"), By.id("buttonViewInvitation")};

        userButtons[0] = testAdminButtons;
        userButtons[1] = disabledUserButtons;
        userButtons[2] = inactivatedUserButtons;

        // Check the values of each user
        while(iterator.hasNext()) {
            WebElement currentElement = iterator.next();
            WebElement id = basics.findWebElementWithin(currentElement, By.id("tableEntryUserName"), "No id found for user in table.");
            for (int i = 0; i < numberExampleUsers; i++) {
                if (id.getText() == userTextValues[i][0]) {
                    WebElement username = basics.findWebElementWithin(currentElement, By.id("tableEntryUserName"), "No user name found for user in table.");
                    WebElement status = basics.findWebElementWithin(currentElement, By.id("tableEntryStatus"), "No status found for user in table.");
                    WebElement fullname = basics.findWebElementWithin(currentElement, By.id("tableEntryFullName"), "No name found for user in table.");
                    WebElement email = basics.findWebElementWithin(currentElement, By.id("tableEntryEmail"), "No email found for user in table.");

                    assertEquals(userTextValues[i][1], username.getText(), "Visibile user name not correct.");
                    assertEquals(userTextValues[i][2], status.getText(), "Visibile status not correct.");
                    assertEquals(userTextValues[i][3], fullname.getText(), "Visibile full name not correct.");
                    assertEquals(userTextValues[i][4], email.getText(), "Visibile email not correct.");

                    assertTrue(basics.findWebElementWithin(currentElement, userButtons[i][0], "Specific button not found.").isDisplayed(), "Specific button not visible.");
                    assertTrue(basics.findWebElementWithin(currentElement, userButtons[i][1], "Specific button not found.").isDisplayed(), "Specific button not visible.");
                }
            }
        }
    }

    /**
     * Checks, whether all main elements can not be found on the current page.
     */
    public void verifyingInvisibleElements() {
        int numberEssentialElements = 9;
        By[] searchPaths = new By[numberEssentialElements];
        searchPaths[0] = By.id("userMonitorHeadline");
        searchPaths[1] = By.id("userInviteButton");
        searchPaths[2] = By.id("returnButton");
        searchPaths[3] = By.id("tableHeadId");
        searchPaths[4] = By.id("tableHeadUserName");
        searchPaths[5] = By.id("tableHeadStatus");
        searchPaths[6] = By.id("tableHeadFullName");
        searchPaths[7] = By.id("tableHeadEmail");
        searchPaths[8] = By.id("tableHeadActions");
        
        for (int i = 0; i < numberEssentialElements; i++) {
            basics.doNotFindWebElement(searchPaths[i], "Element with " + searchPaths[i].toString() + " should not be visible.");
        }
    }

    /**
     * Tests if the home button is functional on this page.
     */
    @Test
    public void homeButtonTest() {
        // Leaving the page
        By homeButton = By.id("main-page-button");
        basics.redirect(homeButton, "Home button failed in user management.");

        // Checking if page was left correctly
        verifyingInvisibleElements();
    }

    /**
     * Tests if the logout button is functional on this page.
     */
    @Test
    public void logoutTest() {
        // Leaving the page
        By logoutButton = By.id("logout-button");
        basics.redirect(logoutButton, "Logout button failed in user management.");

        // Checking if page was left correctly
        verifyingInvisibleElements();

        // Required for afterEach
        super.setLogoutWanted(false);
    }

    /**
     * Tests if the return button is functional on this page.
     */
    @Test
    public void returnTest() {
        // Leaving the page
        By returnButton = By.id("returnButton");
        basics.redirect(returnButton, "Return button failed in user management.");

        // Checking if page was left correctly
        verifyingInvisibleElements();
    }
}
