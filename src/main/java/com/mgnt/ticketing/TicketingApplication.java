package com.mgnt.ticketing;

import com.mgnt.ticketing.base.exception.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TicketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingApplication.class, args);
    }
    @Tag(name = "헬로우 월드!", description = "API HealthCheck")
    @RestController
    @RequestMapping("/hello")
    public static class HelloController {

        @Operation(summary = "정상 응답 여부 확인", description = "서버와 통신")
        @GetMapping
        public ApiResult<?> hello() {
            return ApiResult.success("Hello World!");

        }
    }

}
