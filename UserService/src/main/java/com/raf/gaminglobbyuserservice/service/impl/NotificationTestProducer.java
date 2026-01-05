package com.raf.gaminglobbyuserservice.service.impl;

import com.raf.gaminglobbyuserservice.dto.NotificationEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import com.raf.jms.MessageHelper;

import java.util.ServiceLoader;

@Service
public class NotificationTestProducer {

    private JmsTemplate jmsTemplate;
    private MessageHelper messageHelper;

    @Value("${destination.notification}")
    private String destination;

    public NotificationTestProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.messageHelper =
                ServiceLoader.load(MessageHelper.class)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No MessageHelper found"));
    }

    public void sendTestNotification() {
        NotificationEventDto dto = new NotificationEventDto(
                1L,
                "Test notification",
                "Hello from user service"
        );

        String json = messageHelper.createTextMessage(dto);

        jmsTemplate.send(destination, session ->
                session.createTextMessage(json)
        );

        System.out.println("test notification sent");
    }

}
