package de.unimuenster.imi.randimi.controller.helper;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Initializes the web binder for all controllers.
 * @author Daniel Preciado-Marquez
 */
@ControllerAdvice
public class ControllerSetup {

	@InitBinder
	public void initBinder(final WebDataBinder binder) {
		final StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
		binder.registerCustomEditor(String.class, stringTrimmerEditor);
	}

}
