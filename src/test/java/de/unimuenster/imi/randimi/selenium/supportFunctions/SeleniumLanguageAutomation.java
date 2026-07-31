package de.unimuenster.imi.randimi.selenium.supportFunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SeleniumLanguageAutomation {
    
    private SeleniumBaseFunctions basics;
    private String[] languages;

    /**
     * Checks if all shown text labels have a translation.
     * @param basics Basics function to work with Selenium.
     */
    public SeleniumLanguageAutomation(SeleniumBaseFunctions basics) {
        this.basics = basics;

        // Getting all selectable languages
        basics.redirect(By.id("language-select-button"), "Could not access the language selection.");
        List<WebElement> languageWebElements = basics.findSameTypeWebElements(By.className("language-li"), null);
        basics.redirect(By.id("language-select-button"), "Could not close the language selection.");

        // Checking the amount of selectable languages
        int numberLanguages = Integer.parseInt(SeleniumPropUtils.getProperty("language.number"));
        int actualNumberOfLanguages = languageWebElements.size();
        assertEquals(numberLanguages, actualNumberOfLanguages, "Can not select the actual amount of languages.");

        // Filling the data object
        languages = new String[numberLanguages];
        Iterator<WebElement> languageIterator = languageWebElements.iterator();
        int i = 0;
        while (languageIterator.hasNext()) { 
            languages[i] = languageIterator.next().getAttribute("id");
            i++;
        }
    }

    /**
     * Selects the language from the created list.
     * @param languageIndex Index of the language within the list.
     */
    public void selectingLanguage(int languageIndex) {
        basics.useButton(By.id("language-select-button"), "Could not access the language selection.");
        WebElement currentLanguage = basics.findWebElement(By.id(languages[languageIndex]), "Language with id '" + languages[languageIndex] + "' could not be found");
        WebElement currentButton = basics.findWebElementWithin(currentLanguage, By.className("language"), "Button not found in label");
        basics.redirect(currentButton, "Button not usable.");
    }

    /**
     * Searches for all elements with the 'selenium-language-test'-class and checks their text.
     * Note: Only values of visible elements can be found this way.
     * @return All non translated element texts.
     */
    public String findingAndCheckingAllTranslations() {
        // Preparing output
        String missingTranslations = "";

        // Finding all translated labels.
        String labelCssClass = SeleniumPropUtils.getProperty("language.css.labels");
        List<WebElement> translatedElements = basics.findSameTypeWebElements(By.className(labelCssClass), null);

        // Checking all translated labels.
        if (translatedElements != null) {
            Iterator<WebElement> iterator = translatedElements.iterator();
            while(iterator.hasNext()) {
                WebElement currentElement = iterator.next();
                String currentText = "";
                if (isAttributePresent(currentElement, "placeholder")) {
                    currentText = currentElement.getAttribute("placeholder");
                } else if (isAttributePresent(currentElement, "text")) {
                    currentText = currentElement.getText();
                } else if (isAttributePresent(currentElement, "value")) {
                    currentText = currentElement.getAttribute("value");
                } 

                if(currentText.contains("??")) {
                    if(missingTranslations.equals("")) {
                        missingTranslations = "'" + currentText + "'";
                    } else {
                        missingTranslations += ", '" + currentText + "'";
                    }
                }
            }
        }

        return missingTranslations;
    }

    /**
     * Test if all headlines, labels or example texts have a translation into all given languages.
     */
    public void languageCheckLabel() {
        String missingTranslations = "";

        // Using all available languages
        for (int i = 0; i < languages.length; i++) {
            selectingLanguage(i);

            if(missingTranslations.equals("")) {
                missingTranslations = findingAndCheckingAllTranslations();
            } else {
                missingTranslations += ", " + findingAndCheckingAllTranslations();
            }
        }
        if(!missingTranslations.equals("")) {
            fail("Translations missing for elements with current texts: " + missingTranslations);
        }
    }

    /**
     * Checks, if a given attribute to an element ist present and has an actual value.
     * @param element Element in which the attribute is supposed to be located.
     * @param attribute Attribute that shall be found within the WebElement.
     * @return true = is present and has an actual value, false = is not present or has no actual value.
     */
    public boolean isAttributePresent(WebElement element, String attribute) {
        boolean isPresent = false;
        if(attribute.equals("text")) {
            try {
                String value = element.getText();
                if (value != null && !value.isEmpty()) {
                    isPresent = true;
                }
            } catch (Exception e) {}
        } else {
            try {
                String value = element.getAttribute(attribute);
                if (value != null && !value.isEmpty()) {
                    isPresent = true;
                }
            } catch (Exception e) {}
        }
        return isPresent;
    }

    /**
     * Tests whether required textfield produces an error if left empty and whether that error has a translation.
     * @param errorClass Name of error used on the current page.
     * @param pairs Array of textfield search paths and their entries.
     * @param submitButton Search path for submit button.
     */
    public void checkErrorMessages(String errorClass, SeleniumTextfieldPair[] pairs, By submitButton) {
        // Using all available languages
        for (int i = 0; i < languages.length; i++) {
            // Selecting language
            selectingLanguage(i);

            // Finding and checking all translated errors.
            SeleniumTextfieldAutomation auto = new SeleniumTextfieldAutomation(basics, pairs);
            auto.toggleAndCheck(submitButton, errorClass);
        }
    }
}
