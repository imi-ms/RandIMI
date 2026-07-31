package de.unimuenster.imi.randimi.selenium;

import java.util.Iterator;
import java.util.List;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumLanguageAutomation;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumPropUtils;
import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumTextfieldPair;

import de.unimuenster.imi.randimi.selenium.supportFunctions.SeleniumBaseTest;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the main page.
 * Url: "/"
 * @author Anika Herbermann
 */
@TestPropertySource(properties = "spring.flyway.clean-disabled=false")
public class SeleniumMainPageTest extends SeleniumBaseTest{

    private String adminName = SeleniumPropUtils.getProperty("testuser.admin.name");
    private String adminPass = SeleniumPropUtils.getProperty("testuser.admin.pass");

    /**
     * Tests that login is possible and that profile and logout button are existant.
     */
    @Test
    public void loginTest() {
        // Login and test for correct redirect
        basics.login(adminName, adminPass);

        // Checking for login specific elements
        WebElement userDisplay = basics.findWebElement(By.id("user"), "No username displayed after login.");
        String userDisplayName = SeleniumPropUtils.getProperty("testuser.admin.displayName");
        assertEquals(userDisplayName, userDisplay.getText(), "The username was not shown correctly.");

        basics.findWebElement(By.id("logout-button"), "Logout button missing after login.");
    }

    /**
     * Tests whether a logout is possible.
     */
    @Test
    public void logoutTest() {
        // Using logout method
        basics.login(adminName, adminPass);
        basics.logout();

        // Searching for logout specific elements
        basics.doNotFindWebElement(By.id("logout-button"), "Logout button should not be visible after logout.");
        basics.doNotFindWebElement(By.id("manage-user-button"), "Manage user button should not be visible after logout.");
        basics.doNotFindWebElement(By.id("manage-studies-button"), "Manage studies button should not be visible after logout.");
        basics.doNotFindWebElement(By.id("settings-button"), "Settings button should not be visible after logout.");

        // Required for afterEach
        super.setLogoutWanted(false);
    }

    /**
     * Tests whether an admin can access all elements of the webservice.
     * Note: Update the amount of test studies within properties when new ones are added.
     */
    @Test
    public void adminTest() {
        basics.login(adminName, adminPass);

        // Preparing all specific elements
        int numberElements = 3;
        By[] searchPaths = new By[numberElements];
        searchPaths[0] = By.id("manage-user-button");
        searchPaths[1] = By.id("manage-studies-button");
        searchPaths[2] = By.id("settings-button");

        // Making sure, everything is accessable
        basics.redirect(searchPaths[2], "Admin can not access settings.");
        basics.redirect(searchPaths[0], "Admin can not access user management.");
        basics.redirect(searchPaths[1], "Admin can not access study management.");

        // Checking if admin can see all studies
        int numberStudies = Integer.parseInt(SeleniumPropUtils.getProperty("number.studies"));

        List<WebElement> studies = basics.findSameTypeWebElements(By.id("entryId"), "No studies with a section for ids found.");
        if (studies.size() < numberStudies) {
            fail("The test admin can not see all studies.");
        }

        // Checking if all actions are available
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/view?id=')]"), "No button to view the studie.");
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/edit?id=')]"), "No button to edit the studie.");
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/editUsers?id=')]"), "No button to edit the studie users.");
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/activate?id=')]"), "No button to activate the studie.");
        basics.findWebElement(By.xpath("//a[@data-bs-target='#confirmPopup']"), "No button to delete the studie.");
        basics.findWebElement(By.xpath("//a[contains(@href, '/subject-lists')]"), "No button to view the subjects.");

        // Checking if add study button is available
        basics.findWebElement(By.id("addStudyButton"), "Add study button not found.");
    }

    /**
     * Tests whether a randomizer can access only the study management.
     * Note: Update the property entries for the randomizer associated studies. Otherwise test will fail.
     */
    @Test
    public void randomizerTest() {
        String randomizerName = SeleniumPropUtils.getProperty("testuser.randomizer.name");
        String randomizerPass = SeleniumPropUtils.getProperty("testuser.randomizer.pass");
        basics.login(randomizerName, randomizerPass);

        // Checking that other elements are not visible
        basics.doNotFindWebElement(By.id("manage-user-button"), "Manage user button should not be visible to a randomizer.");
        basics.doNotFindWebElement(By.id("settings-button"), "Settings button should not be visible to a randomizer.");

        // Preparing all specific elements
        int numberElements = 1;
        By[] searchPaths = new By[numberElements];
        searchPaths[0] = By.id("manage-studies-button");

        // Making sure, everything is accessable
        basics.redirect(searchPaths[0], "Randomizer can not access study management.");

        // Checking if a randomizer can see only his studies
        String randomizerStudyNames = SeleniumPropUtils.getProperty("testuser.randomizer.studyName");

        if (!randomizerStudyNames.isBlank()) {
            List<WebElement> studies = basics.findSameTypeWebElements(By.id("entryName"), "Studies with a section for names missing.");

            Iterator<WebElement> iterator = studies.iterator();

            boolean otherStudies = false;
            while(iterator.hasNext()) {
                WebElement idElement = iterator.next();
                String id = idElement.getText();

                if(!randomizerStudyNames.contains("S" + id + "E")) {
                    otherStudies = true;
                } else {
                    randomizerStudyNames = randomizerStudyNames.replace("S" + id + "E", "");
                }
            }

            if(otherStudies) {
                fail("There are studies shown, that are not associated with the test randomizer. (Check property values, if the randomizer should be associated.)");
            }
            if(!randomizerStudyNames.isBlank()){
                fail("There are studies missing, that the randomizer is associated with.");
            }
        }

        // Checking if all actions are available
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/view?id=')]"), "No button to view the studie.");
        basics.doNotFindWebElement(By.xpath("//a[contains(@href, 'studies/edit?id=')]"), "No button to edit the studie.");
        basics.doNotFindWebElement(By.xpath("//a[contains(@href, 'studies/editUsers?id=')]"), "No button to edit the studie users.");
        basics.doNotFindWebElement(By.xpath("//a[contains(@href, 'studies/activate?id=')]"), "No button to activate the studie.");
        basics.doNotFindWebElement(By.xpath("//a[@data-bs-target='#confirmPopup']"), "No button to delete the studie.");
        basics.findWebElement(By.xpath("//a[contains(@href, '/subject-lists')]"), "No button to view the subjects.");

        // Checking if add study button is not available
        basics.doNotFindWebElement(By.id("addStudyButton"), "Add study button was found unexpectedly.");
    }

    /**
     * Tests whether a study manager can access only the study management.
     * Note: Update the property entries for the study manager associated studies. Otherwise test will fail.
     */
    @Test
    public void studyManagerTest() {
        String studyManagerName = SeleniumPropUtils.getProperty("testuser.studyManager.name");
        String studyManagerPass = SeleniumPropUtils.getProperty("testuser.studyManager.pass");
        basics.login(studyManagerName, studyManagerPass);

        // Checking that other elements are not visible
        basics.doNotFindWebElement(By.id("manage-user-button"), "Manage user button should not be visible to study manager.");
        basics.doNotFindWebElement(By.id("settings-button"), "Settings button should not be visible to study manager.");

        // Preparing all specific elements
        int numberElements = 1;
        By[] searchPaths = new By[numberElements];
        searchPaths[0] = By.id("manage-studies-button");

        // Making sure, everything is accessable
        basics.redirect(searchPaths[0], "Study manager can't access study management.");

        // Checking if study manager can see all studies
        int numberStudies = Integer.parseInt(SeleniumPropUtils.getProperty("number.studies"));

        List<WebElement> studies = basics.findSameTypeWebElements(By.id("entryName"), "No studies with a section for ids found.");
        if (studies.size() < numberStudies) {
            fail("The test admin can not see all studies.");
        }

        // Checking if all actions are available
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/view?id=')]"), "No button to view the studie.");
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/edit?id=')]"), "No button to edit the studie.");
        basics.doNotFindWebElement(By.xpath("//a[contains(@href, 'studies/editUsers?id=')]"), "No button to edit the studie users.");
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/activate?id=')]"), "No button to activate the studie.");
        basics.findWebElement(By.xpath("//a[@data-bs-target='#confirmPopup']"), "No button to delete the studie.");
        basics.findWebElement(By.xpath("//a[contains(@href, 'studies/') and contains(@href, '/subject-lists')]"), "No button to view the subjects.");

        // Checking if add study button is available
        basics.findWebElement(By.id("addStudyButton"), "Add study button was not found.");
    }

    /**
     * Tests whether a user manager can access only the user management.
     * Note: Update the property entries for the user manager associated studies. Otherwise test will fail.
     */
    @Test
    public void userManagerTest() {
        String userManagerName = SeleniumPropUtils.getProperty("testuser.userManager.name");
        String userManagerPass = SeleniumPropUtils.getProperty("testuser.userManager.pass");
        basics.login(userManagerName, userManagerPass);

        // Checking that other elements are not visible
        basics.doNotFindWebElement(By.id("settings-button"), "Settings button should not be visible to user manager.");

        // Preparing all specific elements
        int numberElements = 2;
        By[] searchPaths = new By[numberElements];
        searchPaths[0] = By.id("manage-user-button");
        searchPaths[1] = By.id("manage-studies-button");

        // Making sure, everything is accessable
        basics.redirect(searchPaths[0], "User manager can not access user management.");
        basics.redirect(searchPaths[1], "User manager can not access user management.");

        // Checking if a user manager cant see any studies
        basics.doNotFindWebElement(By.id("entryName"), "Studies found.");

        // Checking if add study button is not available
        basics.doNotFindWebElement(By.id("addStudyButton"), "Add study button was found unexpectedly.");
    }

    /**
     * Tests whether the api user can not access the graphical interface of the web service.
     */
    @Test
    public void apiUserTest() {
        String apiUserName = SeleniumPropUtils.getProperty("testuser.apiUser.name");
        String apiUserPass = SeleniumPropUtils.getProperty("testuser.apiUser.pass");

        // Enter input
        SeleniumTextfieldPair name = new SeleniumTextfieldPair(By.id("user"), apiUserName);
        SeleniumTextfieldPair pass = new SeleniumTextfieldPair(By.id("pass"), apiUserPass);

        basics.findWebElementAndWrite(name, "Could not write name.");
        basics.findWebElementAndWrite(pass, "Could not write pass.");

        // Use button
        basics.useButton(By.id("submit"), "Submit button not usable.");

        // Check if login failed
        basics.findWebElement(By.id("errorDiv"), "An error should be displayed.");

        // Required for afterEach
        super.setLogoutWanted(false);
    }

    /**
     * Tests, whether all labels, headlines or example text possess a translation.
     */
    @Override
    @Test
    public void languageTest() {
        SeleniumLanguageAutomation autoLanguageTest = new SeleniumLanguageAutomation(basics);
        // Testing labels, headlines and example texts
        autoLanguageTest.languageCheckLabel();

        // Login to redirect from login page to main page
        basics.login(adminName, adminPass);

        // Testing labels, headlines and example texts again
        autoLanguageTest.languageCheckLabel();
    }

    /**
     * Tests the error messages for required textfields.
     */
    @Test
    public void missingInputTest() {
        // Leaving out entries
        SeleniumTextfieldPair user = new SeleniumTextfieldPair(By.name("user"), adminName);
        SeleniumTextfieldPair pass = new SeleniumTextfieldPair(By.name("pass"), adminPass);
        SeleniumTextfieldPair[] textfields = {user, pass};

        By submit = By.name("submit");

        SeleniumLanguageAutomation automation = new SeleniumLanguageAutomation(basics);
        automation.checkErrorMessages("error-message", textfields, submit);

        // Required for afterEach
        super.setLogoutWanted(false);
    }

    /**
     * Tests whether the login page and the main page show all specific elements.
     */
    @Test
    public void visibilityTest() {
        // Check for web service version
        basics.checkForVersion(randimiVersion);

        // Preperation and search for login page HTML elements
        int numberElements = 5;
        By[] searchPaths = new By[numberElements];

        searchPaths[0] = By.id("logoLoginPage");
        searchPaths[1] = By.id("user");
        searchPaths[2] = By.id("pass");
        searchPaths[3] = By.id("submit");
        searchPaths[4] = By.id("passForgotten");

        WebElement[] elements = basics.findWebElements(searchPaths, "Required elements not found.");

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

        // Login to redirect from the login page to the main page
        basics.login(adminName, adminPass);

        // Test all elements on the main page after login
        var element = basics.findWebElement(By.id("logoMainPage"), "Logo not found.");
        assertTrue(element.isDisplayed(), "Logo not displayed.");
    }

    /**
     * Tests, if the page crashes, when the home button is used on the main page and on the login page.
     */
    @Test
    public void homeButtonTest() {
        basics.redirect(By.id("main-page-button"), "Could not use home button.");
        basics.login(adminName, adminPass);
        basics.redirect(By.id("main-page-button"), "Could not use home button.");
    }

    private void testStudies() {
        String randomizerStudyIds = SeleniumPropUtils.getProperty("testuser.randomizer.studyId");

        if (!randomizerStudyIds.isBlank()) {
            List<WebElement> studies = basics.findSameTypeWebElements(By.id("entryName"), "Studies with a section for names missing.");

            Iterator<WebElement> iterator = studies.iterator();

            boolean otherStudies = false;
            while(iterator.hasNext()) {
                WebElement idElement = iterator.next();
                String id = idElement.getText();

                if(!randomizerStudyIds.contains("S" + id + "E")) {
                    otherStudies = true;
                } else {
                    randomizerStudyIds = randomizerStudyIds.replace("S" + id + "E", "");
                }
            }

            if(otherStudies) {
                fail("There are studies shown, that are not associated with the test randomizer. (Check property values, if the randomizer should be associated.)");
            }
            if(!randomizerStudyIds.isBlank()){
                fail("There are studies missing, that the randomizer is associated with.");
            }
        }
    }
}
