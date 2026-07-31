package de.unimuenster.imi.randimi.selenium.supportFunctions;

import lombok.Getter;
import lombok.Setter;

import org.openqa.selenium.By;

/**
 * A class for a combination of the search path to a textfield and its supposed text input.
 * @author Anika Herbermann
 */
public class SeleniumTextfieldPair {
    
    @Getter
    @Setter
    private String value;

    @Getter
    private By searchPath;

    public SeleniumTextfieldPair(By searchPath, String value) {
        this.searchPath = searchPath;
        this.value = value;
    }
}
