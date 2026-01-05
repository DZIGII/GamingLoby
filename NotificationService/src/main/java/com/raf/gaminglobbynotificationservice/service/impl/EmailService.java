package com.raf.gaminglobbynotificationservice.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sentEmail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Gaming Session invite");
        message.setText(
                "You’ve been invited to join a gaming session.\n\n" +
                        "Click the link below:\n" +
                        link
        );

        mailSender.send(message);
    }

}
