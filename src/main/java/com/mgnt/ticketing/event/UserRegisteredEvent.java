package com.mgnt.ticketing.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRegisteredEvent {
    private String email;
    private String name;
}
