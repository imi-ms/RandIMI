package de.unimuenster.imi.randimi.selenium.supportFunctions;

import static org.junit.jupiter.api.Assertions.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author Anika Herbermann
 */
public class SeleniumTextfieldAutomation {
    
    private SeleniumBaseFunctions basics;
    private SeleniumTextfieldPair[] textfields;
    private WebElement[] textfieldElements;

    /**
     * Checks if all required texfields actually require to be filled.
     * @param basics Basics function to work with Selenium.
     * @param textfields Array of textfield search paths and their entries.
     */
    public SeleniumTextfieldAutomation(SeleniumBaseFunctions basics, SeleniumTextfieldPair[] textfields) {
        this.basics = basics;
        this.textfields = textfields;
        this.textfieldElements = new WebElement[textfields.length];
    }

    /**
     * Iterates through all textfields and checks if error messages are displayed correctly.
     * @param submit Search path to submit button.
     * @param errorClass Class name for the expected error message.
     */
    public void toggleAndCheck(By submit, String errorClass){
        for (int i = 0; i < textfields.length; i++) {
            // Loading all textfields
            textfieldElements = basics.findWebElementsAndWrite(textfields, "Textfield automation could not find or use given elements.");

            // Submitting textfields with one empty
            textfieldElements[i].clear();
            basics.redirect(submit, "Cannot use the submit button to test for presence of error messages.");

            // Checking if an error message is displayed
            String failMessage = "Error Message for textfield '" + textfields[i].getSearchPath().toString() + "' should have been shown.";
            WebElement errorMessage = basics.findWebElement(By.className(errorClass), failMessage);

            // Test for translation
            if (errorMessage.getText().contains("??")) {
                fail("Textfield '" + textfields[i].getSearchPath().toString() + "' is missing a translation.");
            } 
        }
    }
}
