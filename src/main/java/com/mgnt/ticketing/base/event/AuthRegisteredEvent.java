package com.mgnt.ticketing.base.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthRegisteredEvent {
    private String email;
    private String name;
}
