package de.unimuenster.imi.randimi.controller.helper;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adding messages from redirected requests to the model.
 */
@ControllerAdvice
public class MessageAdvice {

	/**
	 * Adding the error message to the model.
	 * @param error Current error string, passed as flush attribute from post-request.
	 * @return The error message.
	 */
	@ModelAttribute("error")
	public String getErrorMessage(@ModelAttribute("error") final String error) {
		return error;
	}

	/**
	 * Adding the success message to the model.
	 * @param success Current success string, passed as flush attribute from post-request.
	 * @return The success message.
	 */
	@ModelAttribute("success")
	public String getSuccessMessage(@ModelAttribute("success") final String success) {
		return success;
	}

}
