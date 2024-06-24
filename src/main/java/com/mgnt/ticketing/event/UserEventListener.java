package com.mgnt.ticketing.event;

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
        try {
            emailService.sendVerificationEmail(event.getEmail(), event.getName());
        } catch (Exception e) {
            log.error("이메일 발송 중 오류 발생: {}", e.getMessage());
            // 필요 시 추가 처리를 여기에 작성
        }
    }
}