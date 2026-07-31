package de.unimuenster.imi.randimi.selenium.supportFunctions;

import java.time.Duration;
import java.util.*;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic selenium functions required for tests.
 * @author Anika Herbermann
 */
public class SeleniumBaseFunctions {

    private final RemoteWebDriver driver;
    private final String baseUrl;


    public SeleniumBaseFunctions(final RemoteWebDriver driver, final String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
    }

    /**
     * Searches for all essential elements of the website.
     * @param failMessage Message that is shown, if the page is missing its main components.
     */
    public void checkForMainComponents(String failMessage) {
        try {
            driver.findElement(By.id("main-page-button"));
            driver.findElement(By.id("language-select-button"));
            driver.findElement(By.id("footer_copyright"));
            driver.findElement(By.id("footer_version"));
        } catch (Exception e) {
            fail(failMessage, e);
        }
    }

    /**
     * Checks if, the displayed version number is the correct one.
     * @param randimiVersion Actual version of the webservice.
     */
    public void checkForVersion(String randimiVersion) {
        WebElement versionElement = findWebElement(By.id("footer_version"), "Version not found.");
        assertEquals("Version: " + randimiVersion, versionElement.getText(), "Incorrect webservice version.");
    };

    /**
     * Uses the given button to redirect to the next page.
     * Can be used as an alternative to the useButton-Method.
     * @param redirectSearchPath By object that finds the requested redirect button.
     * @param failMessage Message that is shown, if the button is not usable or the page can not be loaded correctly.
     */
    public void redirect(By redirectSearchPath, String failMessage) {
        WebElement redirectButton = findWebElement(redirectSearchPath, "Button could not be found.");
        redirect(redirectButton, failMessage);
    }

    /**
     * Uses the given button to redirect to the next page.
     * Can be used as an alternative to the useButton-Method.
     * @param redirectButton By object that finds the requested redirect button.
     * @param failMessage Message that is shown, if the button is not usable or the page can not be loaded correctly.
     */
    public void redirect(WebElement redirectButton, String failMessage) {
        try {
            redirectButton.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            fail(failMessage);
        }
        acceptAlert();
        checkForMainComponents(failMessage);
    }

    /**
     * Finds and uses the requested button with the 'click()' option.
     * @param buttonSearchPath By object that finds the requested button.
     * @param failMessage Message that is shown, when the button can not be found or used.
     */
    public void useButton(By buttonSearchPath, String failMessage) {
        WebElement button = findWebElement(buttonSearchPath, failMessage + " Button not found.");
        useButton(button, failMessage);
    }

    /**
     * Uses the requested button with the 'click()' option.
     * @param button Button WebElement, that shall be used.
     * @param failMessage Message, that is shown, when the button can not be used.
     */
    public void useButton(WebElement button, String failMessage) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(button).click().perform();
        } catch (Exception e) {
            fail(failMessage, e);
        }
    }

    /**
     * Logs into the main page with the given login data.
     */
    public void login(String username, String password) {
        // Setting up input
        try {
            driver.findElement(By.name("user")).sendKeys(username);
            driver.findElement(By.name("pass")).sendKeys(password);
        } catch (Exception e) {
            fail("Login Textfields not interactable.");
        }
        redirect(By.name("submit"), "Login button not useable.");

        // Checking if login was successful
        WebElement failMessage = null;
        try {
            failMessage = driver.findElement(By.id("errorDiv"));
        } catch (Exception e) {}

        if (failMessage != null && failMessage.isDisplayed()) {
            fail("Login data was not accepted.");
        }
    }

    /**
     * Logs out from the current user.
     */
    public void logout() {
        try {
            driver.findElement(By.id("logout-button")).sendKeys(Keys.ENTER);
        } catch (Exception e) {
            fail("Could not perform logout.");
        }
        acceptAlert();
        waitForVisibility(By.id("login-inputs"));
    }

    /**
     * Sets the driver onto the main page.
     */
    public void loadMainPage() {
        driver.get(this.baseUrl);
    }

    /**
     * Sets the driver to a given URL.
     * @param goalURL URL to the requested page.
     */
    public void loadPage(final String goalURL) {
        driver.get(this.baseUrl + goalURL);
    }

    /**
     * Searches within the current page for the requested WebElement.
     * @param searchPath By object that finds the requested WebElement.
     * @param failMessage String that describes the case in which the search fails.
     * @return Found WebElement.
     */
    public WebElement findWebElement(By searchPath, String failMessage) {
        WebElement foundElement = null;
        try {
            foundElement = driver.findElement(searchPath);
        } catch (Exception e) {
            fail(failMessage + " Search path: " + searchPath.toString(), e);
        }
//        waitForVisibility(foundElement);
//        moveToElement(foundElement);
        return foundElement;
    }


    /**
     * Searches for a WebElement, that is not supposed to be found.
     * @param searchPath By object that identifies the unwanted WebElement.
     * @param failMessage String that describes the case, in which the element can be found.
     */
    public void doNotFindWebElement(By searchPath, String failMessage) {
        boolean notFound = false;
        try {
            driver.findElement(searchPath);
        } catch (Exception e) {
            notFound = true;
        }
        if(!notFound) {
            fail(failMessage);
        }
    }

    public void doNotFindWebElements(By[] searchPaths, String failMessage) {
	    for (By searchPath : searchPaths) {
		    doNotFindWebElement(searchPath, failMessage);
	    }
    }

    /**
     * Searches within the given WebElement for the requested WebElement.
     * @param superElement Element, in which another WebElement should be found.
     * @param searchPath By object that finds the requested WebElement.
     * @param failMessage String that describes the case in which the search fails.
     * @return Found WebElement.
     */
    public WebElement findWebElementWithin(WebElement superElement, By searchPath, String failMessage) {
        WebElement foundElement = null;
        try {
            foundElement = superElement.findElement(searchPath);
        } catch (Exception e) {
            fail(failMessage, e);
        }
        return foundElement;
    }

    /**
     * Replaces the text within a given textfield.
     * @param textfield WebElement in which to write.
     * @param text String which contains the new text input.
     */
    public void writeInTextfield(WebElement textfield, String text) {
        try {
            textfield.clear();
            textfield.sendKeys(text);
        } catch (Exception e) {
            fail("Could not write '" + text + "' in textfield with id '" + textfield.getAttribute("id") + "'.", e);
        }
    }

    /**
     * Finds a WebElement and replaces its text value.
     * @param textfieldPair By object that finds the requested element and its text input.
     * @param failMessage String that describes the case in which the search fails.
     * @return Found WebElement.
     */
    public WebElement findWebElementAndWrite(SeleniumTextfieldPair textfieldPair, String failMessage) {
        WebElement textfield = findWebElement(textfieldPair.getSearchPath(), failMessage);
        writeInTextfield(textfield, textfieldPair.getValue());
        return textfield;
    }

    /**
     * Finds multiple WebElements.
     * @param searchPaths By objects that find the requested WebElements.
     * @param failMessage String that describes the case in which a search fails.
     * @return Found WebElements.
     */
    public WebElement[] findWebElements(By[] searchPaths, String failMessage) {
        if(searchPaths.length <= 0) {
            fail("Search paths not set up correctly for element search.");
        }
        WebElement[] elements = new WebElement[searchPaths.length];
        for (int i = 0; i < searchPaths.length; i++) {
            elements[i] = findWebElement(searchPaths[i], failMessage);
        }
        return elements;
    }

    public Map<String, WebElement> findWebElements(final Map<String, By> searchPaths, final String failMessage) {
        final Map<String, WebElement> elements = new HashMap<>();
        for (final var entry : searchPaths.entrySet()) {
            elements.put(entry.getKey(), findWebElement(entry.getValue(), failMessage));
        }
        return elements;
    }

    /**
     * Finds multiple WebElements.
     * @param pairs Textfield pairs that contain the search path to element.
     * @param failMessage String that describes the case in which a search fails.
     * @return Found WebElements.
     */
    public WebElement[] findWebElements(SeleniumTextfieldPair[] pairs, String failMessage) {
        if(pairs.length <= 0) {
            fail("Search paths not set up correctly for element search.");
        }
        WebElement[] elements = new WebElement[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            elements[i] = findWebElement(pairs[i].getSearchPath(), failMessage);
        }
        return elements;
    }

    public WebElement[] findWebElementsWithin(WebElement parent, By[] searchPaths, String failMessage) {
        if(searchPaths.length <= 0) {
            fail("Search paths not set up correctly for element search.");
        }
        WebElement[] elements = new WebElement[searchPaths.length];
        for (int i = 0; i < searchPaths.length; i++) {
            elements[i] = findWebElementWithin(parent, searchPaths[i], failMessage);
        }
        return elements;
    }

    /**
     * Finds multiple WebElements and replaces their text values.
     * @param textfieldPairs TextfieldPairs with identifying By objects and new text input.
     * @param failMessage String that describes the case in which a search fails.
     * @return Found WebElements.
     */
    public WebElement[] findWebElementsAndWrite(SeleniumTextfieldPair[] textfieldPairs, String failMessage) {
        if(textfieldPairs.length <= 0) {
            fail("Search paths not set up correctly for element search.");
        }
        WebElement[] elements = new WebElement[textfieldPairs.length];
        for (int i = 0; i < textfieldPairs.length; i++) {
            elements[i] = findWebElementAndWrite(textfieldPairs[i], failMessage);
        }
        return elements;
    }

    /**
     * Searches for all elements with the same search path.
     * @param searchPath By object that finds the requested WebElements.
     * @param failMessage String that describes the case in which no elements are found. Can be set to null, if the list is allowed to be null.
     * @return Found WebElements as List.
     */
    public List<WebElement> findSameTypeWebElements(By searchPath, String failMessage) {
        List<WebElement> elements = driver.findElements(searchPath);
        if (elements.isEmpty()) {
            if (failMessage != null) {
                fail(failMessage);
            }
            return null;
        } else {
            return elements;
        }
    }

    /**
     * Searches for all elements with the same search path within a parent element.
     * @param parent Element to search within.
     * @param searchPath By object that finds the requested WebElements.
     * @param failMessage String that describes the case in which no elements are found. Can be set to null, if the list is allowed to be null.
     * @return Found WebElements as List.
     */
    public List<WebElement> findSameTypeWebElementsWithin(WebElement parent, By searchPath, String failMessage) {
        List<WebElement> elements = parent.findElements(searchPath);
        if (elements.isEmpty()) {
            if (failMessage != null) {
                fail(failMessage);
            }
            return null;
        } else {
            return elements;
        }
    }


    /**
     * Waits until the element found by the given locator is visible or the timeout is reached.
     * @param  searchPath The locator to wait for.
     * @param timeOutInSeconds The timeout in seconds when an exception is called.
     */
    public WebElement waitForVisibility(final By searchPath, final long timeOutInSeconds) {
        try {
            WebDriverWait block = new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds));
            return block.until(ExpectedConditions.visibilityOfElementLocated(searchPath));
        } catch (Exception e) {
            fail("Element is not visible!", e);
            return null;
        }
    }

    /**
     * Waits until the given web element is visible or the timeout is reached.
     * @param webElement The WebElement to wait for.
     * @param timeOutInSeconds The timeout in seconds when an exception is called.
     */
    public void waitForVisibility(final WebElement webElement, final long timeOutInSeconds) {
        try {
            WebDriverWait block = new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds));
            block.until(ExpectedConditions.visibilityOf(webElement));
        } catch (Exception e) {
            fail("Element is not visible!", e);
        }
    }

    /**
     * Moves to the given element.
     * @param webElement The WebElement to move to.
     */
    public void moveToElement(WebElement webElement) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(webElement).perform();
        } catch (Exception e) {
            fail("Could not move to element!", e);
        }
    }

    /**
     * Waits until the element found by the given locator is visible or the timeout of 10 seconds is reached.
     *
     * @param searchPath The locator of the WebElement to wait for.
     */
    public WebElement waitForVisibility(final By searchPath) {
        return waitForVisibility(searchPath, 10);
    }

    /**
     * Waits until the given web element is visible or the timeout of 10 seconds is reached.
     * @param webElement The WebElement to wait for.
     */
    public void waitForVisibility(final WebElement webElement) {
        waitForVisibility(webElement, 10);
    }

    /**
     * Waits until the given web element is removed from the DOM or the timeout of 10 seconds is reached.
     *
     * @param webElement The WebElement to wait for removal.
     */
    public void waitForStaleness(final WebElement webElement) {
        try {
            final WebDriverWait block = new WebDriverWait(driver, Duration.ofSeconds(10));
            block.until(ExpectedConditions.stalenessOf(webElement));
        } catch (final Exception e) {
            fail("Element is still visible!", e);
        }
    }

    public void acceptAlert() {
        try {
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {
        }
    }


    /**
     * Builds a map with the given elements as keys and search expression of HTML elements with a matching ID property.
     * @param ids IDs of the HTML elements.
     * @return Map containing IDs and their search expression.
     */
    public Map<String, By> buildById(final String... ids) {
        final Map<String, By> result = new HashMap<>();

        for (final String id : ids) {
            result.put(id, By.id(id));
        }

        return result;
    }

    /**
     * Checks if all elements are visible.
     * @param elements The elements to be checked.
     */
    public void checkVisibility(final Map<String, WebElement> elements) {
        final List<String> invisibleElements = new ArrayList<>();
        for (final var entry : elements.entrySet()) {
            if(!entry.getValue().isDisplayed()) {
                invisibleElements.add(entry.getValue().toString());
            }
        }

        assertTrue(invisibleElements.isEmpty(),
                   "Elements with search paths '" + String.join(", ", invisibleElements) + "' cannot be seen.");
    }
}
