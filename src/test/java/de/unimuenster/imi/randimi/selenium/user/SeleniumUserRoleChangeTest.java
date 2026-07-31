package de.unimuenster.imi.randimi.selenium.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.List;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;

/**
 * Tests for the role change page of the user management.
 * Url: "/users/editRoles"
 * @author Anika Herbermann
 */
public class SeleniumUserRoleChangeTest extends SeleniumBaseTest{

    private String testUserName = SeleniumPropUtils.getProperty("testuser.admin.name");
    private String testUserPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

    private int numberFalseCheckboxElements;
    private By[] falseCheckboxElements;
    
    /**
     * Initialises the driver and basic functions and navigates to role change page.
     */
    @BeforeEach
    public void setup() {
        // Setup on required page
        basics.login(testUserName, testUserPass);
        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");
    
        findRoleChangeTestUserManager();
    }

    /**
     * Searches for the user manager within the table and clicks on its role change button.
     */
    public void findRoleChangeTestUserManager() {
        // Finding table entries
        List<WebElement> tableEntries = basics.findSameTypeWebElements(By.id("tableEntry"), "User missing in user overview.");

        // Finding the test user manager within the table
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

        // Picking role change for the test user manager
        if (userName.equals(userManagerUserName)) {
            failMessage = "Role change button not found.";
            try {
                basics.findWebElementWithin(currentElement, By.id("buttonEditUserRoles"), failMessage).sendKeys(Keys.ENTER);
            } catch (Exception e) {
                fail("Role change button not usable.");
            }
        }
    }

    @Test
    public void roleChangeTest() {
        // Changing the role manager to a study manager
        int numberElements = 3;
        By[] boxes = new By[numberElements];
        boxes[0] = By.id("userRoles_ROLE_USER_MANAGER");
        boxes[1] = By.id("userRoles_ROLE_STUDY_MANAGER");

        basics.useButton(boxes[0], "Checkbox for role user manager not usable.");
        basics.useButton(boxes[1], "Checkbox for role study manager not usable.");

        // Saving changes
        By save = By.id("saveButton");
        basics.redirect(save, "Save button not usable.");

        // Check, if role change page was updated.
        findRoleChangeTestUserManager();

        boxes[2] = By.id("userRoles_ROLE_ADMIN");

        WebElement[] elements = basics.findWebElements(boxes, "Boxes not found.");

        assertFalse(elements[0].isSelected(), "The user manager checkbox should not be checked.");
        assertTrue(elements[1].isSelected(), "The study manager checkbox should be checked.");
        assertFalse(elements[2].isSelected(), "The admin checkbox should not be checked.");

        // Check if the old user manager can not access the user overview page any longer
        basics.logout();

        String username = SeleniumPropUtils.getProperty("testuser.userManager.name");
        String password = SeleniumPropUtils.getProperty("testuser.userManager.pass");

        basics.login(username, password);

        basics.doNotFindWebElement(By.id("manage-user-button"), "The manage user button should not be visible the new study manager.");
    }

    @Test
    public void visibilityTest() {
        // Preperation and search for HTML elements
        int numberElements = 8;
        By[] searchPaths = new By[numberElements];
        
        searchPaths[0] = By.id("headline");
        searchPaths[1] = By.tagName("legend");
        searchPaths[2] = By.id("userRoles_ROLE_API_USER");
        searchPaths[3] = By.id("userRoles_ROLE_STUDY_MANAGER");
        searchPaths[4] = By.id("userRoles_ROLE_USER_MANAGER");
        searchPaths[5] = By.id("userRoles_ROLE_ADMIN");
        searchPaths[6] = By.id("saveButton");
        searchPaths[7] = By.id("cancelButton");

        WebElement[] elements = basics.findWebElements(searchPaths, "Required user role change elements not found.");

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

        // Check, if displayed user data is correct
        String username = SeleniumPropUtils.getProperty("testuser.userManager.name").toUpperCase();

        assertTrue(elements[0].getText().contains(username), "The username of the test user manager was not shown within the headline.");

        // Check if checkboxes are correctly selected
        assertFalse(elements[2].isSelected(), "The api user checkbox should be selected for the user manager.");
        assertFalse(elements[3].isSelected(), "The study manager checkbox should not be selected for the user manager.");
        assertTrue(elements[4].isSelected(), "The user manager checkbox should be selected for the user manager.");
        assertFalse(elements[5].isSelected(), "The admin checkbox should not be selected for the user manager.");
    }

    /**
     * Inserts values into checkboxes of the user role change page, that should not be saved on cancel.
     */
    public void prepareFalseInput() {
        // Searching for checkboxes
        numberFalseCheckboxElements = 4;
        falseCheckboxElements = new By[numberFalseCheckboxElements];
        falseCheckboxElements[0] = By.id("userRoles_ROLE_STUDY_MANAGER");
        falseCheckboxElements[1] = By.id("userRoles_ROLE_USER_MANAGER");
        falseCheckboxElements[2] = By.id("userRoles_ROLE_ADMIN");
        falseCheckboxElements[3] = By.id("userRoles_ROLE_API_USER");

        // Accidentally turning the user manager into a study manager
        basics.useButton(falseCheckboxElements[1], "Checkbox for role user manager not usable.");
        basics.useButton(falseCheckboxElements[0], "Checkbox for role study manager not usable.");
    }

    /**
     * Makes sure all checkboxes with false input are not on the page any longer.
     */
    public void verifyingInvisibleElements() {
        for (int i = 0; i < numberFalseCheckboxElements; i++) {
            basics.doNotFindWebElement(falseCheckboxElements[i], "The checkboxes within user role change should not be visible.");
        }
    }

    /**
     * Checks if the values set up in 'prepareFalseInput()' were accidentally saved.
     */
    public void checkForFalseInput() {
        WebElement[] elements = basics.findWebElements(falseCheckboxElements, "Required user role change elements not found.");

        assertEquals(false, elements[0].isSelected(), "The study manager checkbox should not be selected.");
        assertEquals(true, elements[1].isSelected(), "The user manager checkbox should be selected.");
        assertEquals(false, elements[2].isSelected(), "The admin checkbox should not be selected.");
        assertEquals(false, elements[3].isSelected(), "The api user checkbox should not be selected.");
    }

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
    
        findRoleChangeTestUserManager();

        // Checking if false input was accidentally saved
        checkForFalseInput();
    }

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
        By userManagementButton = By.id("manage-user-button");
        basics.redirect(userManagementButton, "Cannot redirect to user management page.");
    
        findRoleChangeTestUserManager();

        // Checking if false input was accidentally saved
        checkForFalseInput();
    }

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
    
        findRoleChangeTestUserManager();

        // Checking if false input was accidentally saved
        checkForFalseInput();
    }
}
