package de.unimuenster.imi.randimi.dto.settings;

import de.unimuenster.imi.randimi.model.enumeration.GravatarDefaultOption;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO of the Settings class.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */
@Setter @Getter
public class SettingsDTO {

	private Long id = 0L;

	private SupportedLanguage defaultLanguage;

	private String mailHost;

	private Integer mailPort;

	private Boolean mailTLS;

	private Boolean mailSMTPAuth;

	private String mailUsername;

	private String mailPassword;

	private String mailSender;

	private String supportMail;

	private String supportPhone;

	private List<PseudonymRegexDTO> pseudonymRegexList = new ArrayList<>();

	//-------------------------------------------------------------------------
	//  Color customization
	//-------------------------------------------------------------------------

	/** Main color of RandIMI. */
	private String mainColor;
	/** Highlight color of RandIMI. */
	private String highlightColor;
	/** Background color of RandIMI. */
	private String backgroundColor;
	/** Main text color of RandIMI. */
	private String mainTextColor;
	/** Highlight text color of RandIMI. */
	private String highlightTextColor;
	
	//-------------------------------------------------------------------------
	//  Gravatar settings
	//-------------------------------------------------------------------------

	// TODO remove values after enabling gravatar again
	/** Enable/Disable Gravatar. */
	private boolean gravatarEnabled = false;
	/** Which is the current default setting? */
	private GravatarDefaultOption gravatarOption = GravatarDefaultOption.MP;

	//-------------------------------------------------------------------------
	//  Footer message settings
	//-------------------------------------------------------------------------	
		
	private List<FooterMessageSettingsDTO> footerMessageSettingsList = new ArrayList<>();
}
