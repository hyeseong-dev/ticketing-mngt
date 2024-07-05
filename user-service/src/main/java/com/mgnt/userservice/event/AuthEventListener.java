package com.mgnt.userservice.event;

import com.mgnt.userservice.domain.service.EmailInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final EmailInterface emailInterface;

    @Async
    @EventListener
    public void handleUserRegisteredEvent(AuthRegisteredEvent event) {
        try {
            emailInterface.sendVerificationEmail(event.getEmail(), event.getName());
        } catch (Exception e) {
            log.error("이메일 발송 중 오류 발생: {}", e.getMessage());
            // 필요 시 추가 처리를 여기에 작성
        }
    }
}