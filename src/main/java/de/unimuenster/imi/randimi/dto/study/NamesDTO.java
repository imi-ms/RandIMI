package de.unimuenster.imi.randimi.dto.study;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@EqualsAndHashCode
@ToString
@Getter @Setter
public class NamesDTO {
	@Nullable
	protected String guiName;
	@Nullable
	private Boolean useApiId = false;
	@Nullable
	protected String apiId;

	@JsonIgnore
	private String originalApiId;

	/**
	 * Checks if this DTO represents an empty form.
	 * @return True if the DTO represents an empty form.
	 */
	@JsonIgnore
	public boolean isFormEmpty() {
		return (guiName == null || guiName.isBlank()) && (apiId == null || apiId.isBlank());
	}
}
