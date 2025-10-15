package com.infosys.smartshelfx_backend.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
        }
    }

    public void sendSms(String to, String body) {
        if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
            System.out.println("Twilio credentials not set; skipping SMS");
            return;
        }
        if (to == null || to.isEmpty()) {
            System.out.println("No destination phone number provided; skipping SMS");
            return;
        }
        Message.creator(new com.twilio.type.PhoneNumber(to), new com.twilio.type.PhoneNumber(fromNumber), body).create();
    }
}
