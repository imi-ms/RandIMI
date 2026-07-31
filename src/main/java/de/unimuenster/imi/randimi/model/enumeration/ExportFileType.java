package de.unimuenster.imi.randimi.model.enumeration;

import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
public enum ExportFileType {
	CSV("text/csv", ".csv"),
	JSON(MediaType.APPLICATION_JSON_VALUE, ".json"),
	;

	private final String contentType;

	private final String fileExtension;

	ExportFileType(final String contentType, final String fileExtension) {
		this.contentType = contentType;
		this.fileExtension = fileExtension;
	}
}
