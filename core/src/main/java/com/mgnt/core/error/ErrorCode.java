package com.mgnt.core.error;

import com.mgnt.core.enums.MessageCommInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements MessageCommInterface {

    // Common Errors
    INVALID_INPUT_VALUE(400, "C001", "Validation failed."),
    METHOD_NOT_ALLOWED(405, "C002", "Method Not Allowed"),
    ENTITY_NOT_FOUND(404, "C003", "Entity Not Found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Internal error."),
    INVALID_TYPE_VALUE(400, "C005", "Invalid Type Value"),
    ACCESS_DENIED(403, "C006", "Do not have permission."),
    BAD_REQUEST(400, "C007", "Bad request."),
    NOT_ACCEPTABLE(406, "C008", "Not acceptable."),
    CONFLICT(409, "C009", "Conflict."),
    PRECONDITION_FAILED(412, "C010", "Precondition failed."),
    PAYLOAD_TOO_LARGE(413, "C011", "Payload too large."),
    UNSUPPORTED_MEDIA_TYPE(415, "C012", "Unsupported media type."),
    TOO_MANY_REQUESTS(429, "C013", "Too many requests."),
    ENDPOINT_NOT_FOUND(404, "C014", "Endpoint not found."),
    HTTP_METHOD_NOT_FOUND(405, "C015", "HTTP method not found."),
    PHONE_NUMBER_DUPLICATED(409, "C017", "Phone number duplicated."),
    CONCURRENT_MODIFICATION(400, "C018", "Concurrent Modification."),

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
    EMAIL_DUPLICATED(409, "A004", "Email duplicated."),
    EMAIL_SEND_ERROR(400, "A005", "EMAIL SENDING_ERROR"),
    VERIFICATION_CODE_NOT_FOUND(400, "A006", "Verification Code not found."),
    INVALID_EMAIL_FORMAT(400, "A007", "Invalid email format"),

    // JWT Errors
    TOKEN_EXPIRED(401, "J001", "Token has expired."),
    TOKEN_INVALID(401, "J002", "Token is invalid."),
    TOKEN_MALFORMED(401, "J003", "Token is malformed."),
    TOKEN_UNSUPPORTED(401, "J004", "Token is unsupported."),
    TOKEN_CLAIM_INVALID(401, "J005", "Token claims are invalid."),
    INVALID_REFRESH_TOKEN(401, "J006", "Invalid refresh token."),

    // Concert
    DATE_IS_NULL(404, "C018", "예정된 콘서트 날짜가 없습니다."),
    CONCERT_NOT_FOUND(404, "C019", "해당 콘서트가 없습니다"),
    CONCERT_DATE_NOT_FOUND(404, "C020", "해당 콘서트 일정이 없습니다"),
    SEAT_NOT_FOUND(404, "C021", "좌석이 없습니다"),

    // Reservation
    RESERVATION_ALREADY_RESERVED(400, "R001", "이미 예약되었습니다"),
    IS_NULL(400, "R002", "예약 정보가 없습니다."),
    RESERVATION_FAILED(400, "R003", "예약 할 수 없습니다."),
    RESERVATION_NOT_FOUND(404, "R004", "예약이 존재 하지 않습니다"),
    RESERVATION_ACCESS_FAILED(400, "R005", "예매가 허용되지 않습니다."),
    RESERVATION_TOKEN_ALREADY_EXIST(400, "R006", "예약 토큰이 존재합니다."),

    // Payment
    INSUFFICIENT_BALANCE(400, "P001", "잔액이 부족합니다."),
    NOT_AVAILABLE_PAY(400, "P002", "결제 가능한 상태가 아닙니다."),
    NOT_AVAILABLE_CANCEL(400, "P003", "취소 가능한 상태가 아닙니다."),
    PAYMENT_NOT_FOUND(404, "P004", "Payment not found."),

    // Seat
    SEAT_NOT_AVAILABLE(400, "S001", "사용 가능한 좌석이 아닙니다."),

    // Payment
    PLACE_NOT_FOUND(404, "P001", "해당 Place를 찾을 수 없습니다."),

    // Inventory
    INVENTORY_NOT_FOUND(404, "I001", "콘서트 티켓이 존재하지 않습니다."),
    INSUFFICIENT_INVENTORY(404, "I002", "콘서트 티켓 재고가 충분하지 않습니다."),

    // Queue
    QUEUE_ENTRY_FAILED(400, "Q001", "대기열 진입에 실패했습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
