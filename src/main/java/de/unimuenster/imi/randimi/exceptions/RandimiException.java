package de.unimuenster.imi.randimi.exceptions;

import de.unimuenster.imi.randimi.model.api.ErrorResponseDetails;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception that may occur in Rest API calls.
 * @author Tobias Brix
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Paul Schaub
 */
public abstract class RandimiException extends Exception {

	public static final String ERROR_CODE_PREFIX = "R";

	public static final int MISSING_PARAMETER = 1000;
	public static final int MISSING_PARAMETER_LOCATION = 1010;
	public static final int MISSING_PARAMETER_PSEUDONYM = 1020;
	public static final int MISSING_PARAMETER_STUDY_ID = 1030;
	public static final int MISSING_PARAMETER_STRATUM = 1040;
	public static final int MISSING_PARAMETER_STRATUM_PARAMS = 1045;
	public static final int MISSING_PARAMETER_SITE_API_ID = 1050;

	public static final int MALFORMED_PARAMETER = 2000;
	public static final int MALFORMED_PARAMETER_PSEUDONYM_REGEX = 2010;
	public static final int MALFORMED_PARAMETER_STRATUM = 2020;
	public static final int MALFORMED_PARAMETER_STRATUM_TYPE = 2030;

	public static final int UNSATISFYING_PARAMETER = 3000;
	public static final int UNSATISFYING_PARAMETER_PSEUDONYM_REGEX_MISMATCH = 3010;

	public static final int DUPLICATE_REQUEST = 4000;
	public static final int DUPLICATE_REQUEST_PSEUDONYM_ALREADY_REGISTERED = 4010;

	public static final int NOT_ACCEPTABLE = 5000;
	public static final int NOT_ACCEPTABLE_MISSING_STUDY = 5010;
	public static final int NOT_ACCEPTABLE_MISSING_SUBJECT_LIST = 5011;
	public static final int NOT_ACCEPTABLE_MISSING_LOCATION = 5015;
	public static final int NOT_ACCEPTABLE_MISSING_SITE = 5017;
	public static final int NOT_ACCEPTABLE_MISSING_RANDOMIZATION_LIST = 5020;
	public static final int NOT_ACCEPTABLE_MISSING_RANDOMIZATION_ENTRY = 5025;
	public static final int NOT_ACCEPTABLE_MISSING_STRATUM = 5030;
	public static final int NOT_ACCEPTABLE_MISSING_STRATUM_PART = 5035;
	public static final int NOT_ACCEPTABLE_MISSING_MATCHING_STRATUM_PART = 5037;
	public static final int NOT_ACCEPTABLE_STUDY_FULL = 5040;
	public static final int NOT_ACCEPTABLE_STRATUM_PART_FULL = 5045;
	public static final int NOT_ACCEPTABLE_SITE_FULL = 5047;
	public static final int NOT_ACCEPTABLE_STUDY_LOCKED = 5050;
	public static final int NOT_ACCEPTABLE_STUDY_NOT_ACTIVE = 5051;
	public static final int NOT_ACCEPTABLE_STUDY_NOT_LOCKED = 5052;
	public static final int NOT_ACCEPTABLE_STUDY_ALREADY_LOCKED = 5053;
	public static final int NOT_ACCEPTABLE_STUDY_NOT_ARCHIVED = 5054;
	public static final int NOT_ACCEPTABLE_STUDY_ACTIVE = 5055;
	public static final int NOT_ACCEPTABLE_STUDY_ARCHIVED = 5056;
	public static final int NOT_ACCEPTABLE_STUDY_DELETED = 5057;

	public static final int INTERNAL_SERVER_ERROR = 6000;
	public static final int INTERNAL_SERVER_ERROR_UNKNOWN_ALGORITHM = 6010;
	public static final int INTERNAL_SERVER_ERROR_UNKNOWN_STRATUM_TYPE = 6020;
	public static final int INTERNAL_SERVER_ERROR_STUDY_NOT_FOUND = 6100;

	@Getter
	private final int errorCode;

	@Getter
	private final String localizedMessage;

	@Getter
	private final ErrorResponseDetails details = new ErrorResponseDetails();

	public RandimiException(int errorCode, String message) {
		super("[" + ERROR_CODE_PREFIX + errorCode + "] " + message);
		this.localizedMessage = message;
		this.errorCode = errorCode;
	}

	public RandimiException(int errorCode, String message, Throwable cause) {
		super("[" + ERROR_CODE_PREFIX + errorCode + "] " + message, cause);
		this.localizedMessage = message;
		this.errorCode = errorCode;
	}

	public abstract HttpStatus getHttpStatusCode();

	public static class BadRequest extends RandimiException {

		public BadRequest(int errorCode, String message) {
			super(errorCode, message);
		}

		public BadRequest(int errorCode, String message, Throwable cause) {
			super(errorCode, message, cause);
		}

		@Override
		public HttpStatus getHttpStatusCode() {
			return HttpStatus.BAD_REQUEST;
		}
	}

	public static class NotAcceptable extends RandimiException {

		public NotAcceptable(int errorCode, String message) {
			super(errorCode, message);
		}

		@Override
		public HttpStatus getHttpStatusCode() {
			return HttpStatus.NOT_ACCEPTABLE;
		}
	}

	public static class Conflict extends RandimiException {

		public Conflict(int errorCode, String message) {
			super(errorCode, message);
		}

		@Override
		public HttpStatus getHttpStatusCode() {
			return HttpStatus.CONFLICT;
		}
	}

	public static class InternalServerError extends RandimiException {

		public InternalServerError(int errorCode, String message) {
			super(errorCode, message);
		}

		@Override
		public HttpStatus getHttpStatusCode() {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}

	}

}
