package com.mgnt.core.event;

public record ErrorEvent(
        Long userId,
        Long concertId,
        String errorMessage
) {
    // 생성자, getter, equals, hashCode, toString 메서드가 자동으로 생성됩니다.

    // 필요한 경우 추가 메서드를 여기에 정의할 수 있습니다.
    public String getFormattedErrorMessage() {
        return String.format("Error for user %d and concert %d: %s", userId, concertId, errorMessage);
    }
}