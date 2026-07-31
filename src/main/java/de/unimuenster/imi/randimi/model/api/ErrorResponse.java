package de.unimuenster.imi.randimi.model.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Response in case of an error.
 */
@Schema(description = "Response in case of an error.")
@Getter @Setter
public class ErrorResponse {

	/**
	 * URL describing the error.
	 */
	@Schema(description = "URL linking to a description of the error.", example = "about:blank",
	        requiredMode = Schema.RequiredMode.REQUIRED)
	private String type = "about:blank";

	/**
	 * Name of the status code.
	 */
	@Schema(description = "Name of the HTTP status code.", example = "BAD_REQUEST",
	        requiredMode = Schema.RequiredMode.REQUIRED)
	private String title;

	/**
	 * Value of the status code.
	 */
	@Schema(description = "Numeric value of the HTTP status code.", example = "400",
	        requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer status;

	/**
	 * URL of the called API.
	 */
	@Schema(description = "URL of the requested endpoint.", example = "/api/v2/study/0/subject",
	        requiredMode = Schema.RequiredMode.REQUIRED)
	private String instance;

	/**
	 * Timestamp of the error.
	 */
	@Schema(description = "Timestamp of the error.", example = "2024-07-02 11:42:27",
	        requiredMode = Schema.RequiredMode.REQUIRED)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	private Date timestamp = new Date();

	/**
	 * Detailed and human-readable message of the error.
	 */
	@Schema(description = "Detailed and human-readable message of the error.",
	        example = "Validation failed. See validationErrors for more details.",
	        requiredMode = Schema.RequiredMode.REQUIRED)
	private String detail;

	/**
	 * Exception message.
	 */
	@Schema(description = "Message of the exception.", example = "2024-07-02 11:42:27",
	        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String errors;

	/**
	 * Details of the error.
	 */
	@Schema(description = "Details of the error.", nullable = true)
	@Nullable
	private ErrorResponseDetails details = null;

	/**
	 * Error Code of the RandimiException.
	 */
	@Schema(description = "Code describing the error.", example = "5017",
	        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private Integer errorCode;
}
