package com.infosys.smartshelfx_backend.service;

// import com.twilio.Twilio;
// import com.twilio.rest.api.v2010.account.Message;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    // private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    // @Value("${twilio.account.sid:}")
    // private String accountSid;

    // @Value("${twilio.auth.token:}")
    // private String authToken;

    // @Value("${twilio.from-number:}")
    // private String fromNumber;

    // @PostConstruct
    // public void init() {
    //     if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
    //         Twilio.init(accountSid, authToken);
    //         logger.info("Twilio initialized");
    //     } else {
    //         logger.warn("Twilio credentials not set; skipping initialization");
    //     }
    // }

    // public void sendSms(String to, String body) {
    //     if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
    //         logger.warn("Twilio credentials not set; skipping SMS");
    //         return;
    //     }
    //     if (to == null || to.isEmpty()) {
    //         logger.warn("No destination phone number provided; skipping SMS");
    //         return;
    //     }
    //     try {
    //         Message.creator(new com.twilio.type.PhoneNumber(to), new com.twilio.type.PhoneNumber(fromNumber), body).create();
    //         logger.info("SMS sent to {}", to);
    //     } catch (Exception e) {
    //         logger.error("Failed to send SMS to {}", to, e);
    //     }
    // }
}
