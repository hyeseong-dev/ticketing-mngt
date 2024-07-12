package com.mgnt.reservationservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SSEService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        log.info("SSE 연결 생성: userId={}", userId);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 완료: userId={}", userId);
        });

        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(userId);
            log.warn("SSE 연결 타임아웃: userId={}", userId);
        });

        return emitter;
    }

    public void sendEvent(SseEmitter emitter, String eventName, Object data) {
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
                log.info("이벤트 전송 성공: eventName={}", eventName);
            } catch (IOException e) {
                log.error("이벤트 전송 실패: eventName={}", eventName, e);
                handleSendError(emitter);
            }
        } else {
            log.warn("SSE 연결을 찾을 수 없음");
        }
    }

    private void handleSendError(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("error").data("데이터 전송 중 오류가 발생했습니다."));
        } catch (IOException ex) {
            log.error("오류 메시지 전송 실패", ex);
        } finally {
            emitter.complete();
        }
    }
}