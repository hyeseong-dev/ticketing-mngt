package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.auth.EmailRequestDto;

public interface EmailService {

    void sendEmail(EmailRequestDto emailMessage);

    void sendVerificationEmail(String email, String name);
}
