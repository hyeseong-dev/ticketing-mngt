package com.mgnt.ticketing.controller.waiting;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대기열", description = "waiting-controller")
@RequestMapping("/waits")
@RestController
@RequiredArgsConstructor
public class WaitingController {
}