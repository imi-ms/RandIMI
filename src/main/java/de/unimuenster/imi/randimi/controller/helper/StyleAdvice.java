
package de.unimuenster.imi.randimi.controller.helper;

import de.unimuenster.imi.randimi.model.settings.Settings;
import de.unimuenster.imi.randimi.repository.settings.SettingsRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

/**
 * Adding current color settings to each model.
 * 
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */
@ControllerAdvice
public class StyleAdvice {
	/*
	 * REMOVE: Use Database Version instead.
	 * Remove property from config.properties
	 * remove filtering in maven-resource plugin.
	 */
	@Value("${app.version}")
    private String version;  
	
	@Autowired
	SettingsRepository dao;
	
	@ModelAttribute
	public void handleRequest(HttpServletRequest request, Model model, Locale locale) {
		Settings settings = dao.getCurrentSettings();
		//colors
		model.addAttribute("mainColor",settings.getMainColor());
		model.addAttribute("highlightColor",settings.getHighlightColor());
		model.addAttribute("backgroundColor",settings.getBackgroundColor());
		model.addAttribute("mainTextColor",settings.getMainTextColor());
		model.addAttribute("highlightTextColor",settings.getHighlightTextColor());
		//gravatar
		model.addAttribute("gravatarEnabled",settings.isGravatarEnabled());
		model.addAttribute("gravatarDefaultOption",settings.getGravatarOption().getUrlEncoding());
		//version
		model.addAttribute("appVersion",version);
		//model.addAttribute("footerMessageSettings",settings.getFooterMessageSettingsList().removeIf(l -> !l.getCurrentLanguage().equals(settings.getDefaultLanguage())));
		model.addAttribute("footerMessageSettings",settings.getFooterMessageSettingsList());
		model.addAttribute("locale",locale);
	}
}
