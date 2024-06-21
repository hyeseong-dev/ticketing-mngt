package com.mgnt.ticketing.dto.response;

public interface ResponseCode {

    // Common
    String SUCCESS = "SU";
    String DATABASE_ERROR = "DBE";
    String NO_PERMISSION = "NP";
    String VALIDATION_FAILED = "VF";

    // User
    String DUPLICATED_EMAIL = "DE";
    String NOT_EXIST_USER = "NU";
    String EMAIL_VERIFICATION_SUCCESS = "EVS";
    String EMAIL_VERIFICATION_FAILED = "EVF";

    // Auth
    String SIGN_UP_SUCCESS = "SUS";
    String SIGN_UP_FAILED = "SUF";
    String SIGN_IN_FAILED = "SF";

}
