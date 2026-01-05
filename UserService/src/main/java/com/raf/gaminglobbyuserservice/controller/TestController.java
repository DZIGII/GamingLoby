package com.raf.gaminglobbyuserservice.controller;

import com.raf.gaminglobbyuserservice.service.impl.NotificationTestProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    private NotificationTestProducer producer;

    public TestController(NotificationTestProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/notify")
    public void test() {
        producer.sendTestNotification();
    }
}
