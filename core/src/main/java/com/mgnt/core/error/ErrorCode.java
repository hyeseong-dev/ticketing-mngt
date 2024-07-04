package com.mgnt.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors
    INVALID_INPUT_VALUE(400, "C001", "Validation failed."),
    METHOD_NOT_ALLOWED(405, "C002", "Method Not Allowed"),
    ENTITY_NOT_FOUND(404, "C003", "Entity Not Found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Internal error."),
    INVALID_TYPE_VALUE(400, "C005", "Invalid Type Value"),
    ACCESS_DENIED(403, "C006", "Do not have permission."),

    // User Errors
    USER_NOT_FOUND(404, "U001", "This user does not exist."),
    EMAIL_ALREADY_EXISTS(409, "U002", "Duplicate email."),
    USER_UNAUTHORIZED(401, "U003", "Unverified email."),
    CURRENT_PASSWORD_NOT_MATCHED(400, "U004", "Current password not matched."),
    NEW_PASSWORD_NOT_MATCHED(400, "U005", "NEW password not matched."),

    // Auth Errors
    LOGIN_FAILED(401, "A001", "Login information mismatch."),
    INVALID_CREDENTIALS(401, "A002", "Invalid Credentials."),
    UNVERIFED_ACCOUNT(401, "A003", "Unverified account."),

    // JWT Errors
    TOKEN_EXPIRED(401, "J001", "Token has expired."),
    TOKEN_INVALID(401, "J002", "Token is invalid."),
    TOKEN_MALFORMED(401, "J003", "Token is malformed."),
    TOKEN_UNSUPPORTED(401, "J004", "Token is unsupported."),
    TOKEN_CLAIM_INVALID(401, "J005", "Token claims are invalid."),

    // Additional Errors
    BAD_REQUEST(400, "C007", "Bad request."),
    NOT_ACCEPTABLE(406, "C008", "Not acceptable."),
    CONFLICT(409, "C009", "Conflict."),
    PRECONDITION_FAILED(412, "C010", "Precondition failed."),
    PAYLOAD_TOO_LARGE(413, "C011", "Payload too large."),
    UNSUPPORTED_MEDIA_TYPE(415, "C012", "Unsupported media type."),
    TOO_MANY_REQUESTS(429, "C013", "Too many requests."),
    ENDPOINT_NOT_FOUND(404, "C014", "Endpoint not found."),
    HTTP_METHOD_NOT_FOUND(405, "C015", "HTTP method not found."),
    EMAIL_DUPLICATED(409, "C016", "Phone number duplicated."),
    PHONE_NUMBER_DUPLICATED(409, "C017", "Email duplicated."),
    EMAIL_SEND_ERROR(400, "C018", "EMAIL SENDING_ERROR");

    private final int status;
    private final String code;
    private final String message;
}
