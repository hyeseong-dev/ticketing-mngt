package com.mgnt.ticketing.event;

import com.mgnt.ticketing.dto.request.auth.UserRegisteredEvent;
import com.mgnt.ticketing.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        emailService.sendVerificationEmail(event.getEmail(), event.getName());
    }
}