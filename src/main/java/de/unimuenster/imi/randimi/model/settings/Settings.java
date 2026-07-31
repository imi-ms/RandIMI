package de.unimuenster.imi.randimi.model.settings;

import de.unimuenster.imi.randimi.model.EntityBase;
import de.unimuenster.imi.randimi.model.enumeration.GravatarDefaultOption;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that holds all server-wide settings, which can be edited as admin user.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Tobias Brix <tobais.brix@uni-muenster.de>
 */
@Entity
public class Settings extends EntityBase {
	
	/** 
	 * Default language of the application.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter @Setter
	private SupportedLanguage defaultLanguage;
	
	/**
	 * Host of the application mailer.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String mailHost;
	
	/**
	 * Port of the application mailer.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private int mailPort;
	
	/**
	 * Enable TLS.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private boolean mailTLS;
	
	/**
	 * SMTP authentication.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private boolean mailSMTPAuth;
	
	/**
	 * Username of the application mailer.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String mailUsername;
	
	/**
	 * Password of the application mailer.
	 */
	@Column
	@Getter @Setter
	private String mailPassword;
	
	/**
	 * Sender of the emails sent from the application mailer.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String mailSender;

	/**
	 * Support email address.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String supportMail;
	
	/**
	 * Support phone number.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private String supportPhone;
	
	/**
	 * List of study arms associated with this study.
	 */
	@OneToMany(mappedBy = "settings", orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("orderNumber")
	@Cascade(CascadeType.ALL)
	@Getter
	private final List<PseudonymRegex> pseudonymRegexList = new ArrayList<>();

	public void addPseudonymRegex(PseudonymRegex pseudonymRegex) {
		pseudonymRegexList.add(pseudonymRegex);
		if (pseudonymRegex.getSettings() != this) {
			pseudonymRegex.setSettings(this);
		}
	}

	public void removePseudonymRegex(PseudonymRegex pseudonymRegex) {
		pseudonymRegexList.remove(pseudonymRegex);
		if (pseudonymRegex.getSettings()!= null) {
			pseudonymRegex.setSettings(null);
		}
	}
	
	//-------------------------------------------------------------------------
	//  Color customization
	//-------------------------------------------------------------------------
	
	/**
	 * Main color of RandIMI.
	 * Null equals default
	 */
	@Column(nullable = true)
	@Getter @Setter
	private String mainColor;
	
	/**
	 * Highlight color of RandIMI
	 * Null equals default
	 */
	@Column(nullable = true)
	@Getter @Setter
	private String highlightColor;
	
	/**
	 * Background color of RandIMI
	 * Null equals default
	 */
	@Column(nullable = true)
	@Getter @Setter
	private String backgroundColor;
	
	/**
	 * Main text color of RandIMI
	 * Null equals default
	 */
	@Column(nullable = true)
	@Getter @Setter
	private String mainTextColor;
	
	/**
	 * Highlight text color of RandIMI
	 * Null equals default
	 */
	@Column(nullable = true)
	@Getter @Setter
	private String highlightTextColor;
	
	//-------------------------------------------------------------------------
	//  Gravatar settings
	//-------------------------------------------------------------------------	
	
	/**
	 * If false, no Gravatar interaction is performed.
	 */
	@Column(nullable = false)
	@Getter @Setter
	private boolean gravatarEnabled;
	
	/**
	 * Defines the behavior of an unregistered Gravatar email.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter @Setter
	private GravatarDefaultOption gravatarOption;
	
	//-------------------------------------------------------------------------
	//  Footer message settings
	//-------------------------------------------------------------------------	
	
	/**
	 * List of study arms associated with this study.
	 */
	@OneToMany(mappedBy = "settings", orphanRemoval = true, fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@Getter
	private final Set<FooterMessageSettings> footerMessageSettingsList = new HashSet<>();

	public void addFooterMessageSettings(FooterMessageSettings footerMessageSettings) {
		footerMessageSettingsList.add(footerMessageSettings);
		if (footerMessageSettings.getSettings()!= this) {
			footerMessageSettings.setSettings(this);
		}
	}

	public void removeFooterMessageSettings(FooterMessageSettings footerMessageSettings) {
		footerMessageSettingsList.remove(footerMessageSettings);
		if (footerMessageSettings.getSettings()!= null) {
			footerMessageSettings.setSettings(null);
		}
	}
	
}
