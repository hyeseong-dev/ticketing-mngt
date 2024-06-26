package com.mgnt.ticketing.domain.concert;

import com.mgnt.ticketing.base.message.MessageCommInterface;
import lombok.Getter;

public class ConcertCustomException extends RuntimeException {
    @Getter
    private final String errorCode;
    private final String message;

    public ConcertCustomException(MessageCommInterface messageCommInterface) {
        super(messageCommInterface.getMessage());
        this.errorCode = messageCommInterface.getCode();
        this.message = messageCommInterface.getMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
