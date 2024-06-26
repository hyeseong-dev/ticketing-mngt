package com.mgnt.ticketing.base.message;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = MessageCommSerializer.class)
public interface MessageCommInterface {
    String getCode();
    String getMessage();
}