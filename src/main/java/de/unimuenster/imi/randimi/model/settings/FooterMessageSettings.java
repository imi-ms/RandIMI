package de.unimuenster.imi.randimi.model.settings;

import de.unimuenster.imi.randimi.model.EntityBase;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that holds the messages of the footer. Namely, Imprint, Data Privacy and Support with all supported languages. 
 * the regex.
 * 
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */
@Entity
public class FooterMessageSettings extends EntityBase {
	
	/**
	 * Associated settings of this message setting.
	 */
	@ManyToOne(optional = false)
	@Getter
	private Settings settings;
	
	public void setSettings(Settings settings) {
		Settings oldSettings = this.settings;
		this.settings = settings;
		if (oldSettings != null && oldSettings.getFooterMessageSettingsList().contains(this)) {
			oldSettings.removeFooterMessageSettings(this);
		}
		if (settings != null && !settings.getFooterMessageSettingsList().contains(this)) {
			settings.addFooterMessageSettings(this);
		}
	}
	
	/**
	 * Language of the message.
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter @Setter
	private SupportedLanguage currentLanguage;
	
	/**
	 * Content of the imprint.
	 */
	@Column(columnDefinition="TEXT")
	@Getter @Setter
	private String imprintContent;
	
	/**
	 * Content of the data privacy.
	 */
	@Column(columnDefinition="TEXT")
	@Getter @Setter
	private String dataPrivacyContent;
	
		/**
	 * Content of the message.
	 */
	@Column(columnDefinition="TEXT")
	@Getter @Setter
	private String supportContent;
	
}
