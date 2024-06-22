package com.mgnt.ticketing.dto.response;

public interface ResponseMessage {

    // Common
    String SUCCESS = "Success.";
    String DATABASE_ERROR = "Database error.";
    String NO_PERMISSION = "Do not have permission.";
    String VALIDATION_FAILED = "Validation failed.";

    // User
    String DUPLICATED_EMAIL = "Duplicate email.";
    String NOT_EXIST_USER = "This user does not exist.";
    String EMAIL_VERIFICATION_SUCCESS = "Email verification successful.";
    String EMAIL_VERIFICATION_FAILED = "Email verification failed.";

    // Auth
    String UNVERIFIED_EMAIL = "Unverified email.";
    String SIGN_UP_SUCCESS = "Sign up successful.";
    String LOGIN_SUCCESS = "Login successful.";
    String LOGOUT_SUCCESS = "Logout successful.";
    String LOGIN_FAILED = "Login information mismatch.";
    String INVALID_CREDENTIALS = "Invalid email or password.";  // 추가
    String INVALID_REQUEST = "Invalid request";

    //SERVER
    String INTERNAL_ERROR = "Internal error.";
}
