package de.unimuenster.imi.randimi.model.enumeration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;


/**
 * Options used in to handle default Icons, if the email is not valid.
 * 
 * @author Tobias Brix
 */
public enum GravatarDefaultOption {
	MP("MP","mp","mp.jpg"),
	IDENTICON("Identicon","identicon","identicon.png"),
	MONSTERID("MonsterID","monsterid","monsterid.png"),
	WAVATAR("Wavatar","wavatar","wavatar.png"),
	RETRO("Retro","retro","retro.png"),
	ROBOHASH("Robohash","robohash","robohash.png");

	/** Display name used in settings. */
	@Getter
	private final String displayName;
	/** Encoding used in the Gravatar url. */
	@Getter
	private final String urlEncoding;
	/** Url of the preview image. */
	private final String previewUrl;

	/** Constructor. */
	private GravatarDefaultOption(String displayName, String urlEncoding, String previewUrl) {
		this.displayName = displayName;
		this.urlEncoding = urlEncoding;
		this.previewUrl  = previewUrl;
	}

	public String getPreviewUrl() {
		String prefix = "/resources/images/gravatar/";
		return prefix + this.previewUrl;
	}

	/** 
	 * Helper for list of display names.
	 * @return List of display names.
	 */
	public static List<String> getDisplayNames() {
		return Stream.of(GravatarDefaultOption.values()).map(GravatarDefaultOption::getDisplayName).collect(Collectors.toList());
	}
	/** 
	 * Helper for list of preview urls.
	 * @return List of preview urls.
	 */
	public static List<String> getPreviewUrls() {
		return Stream.of(GravatarDefaultOption.values()).map(GravatarDefaultOption::getPreviewUrl).collect(Collectors.toList());
	}

}
