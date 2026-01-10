package com.raf.gaminglobbynotificationservice.listener;

import com.raf.gaminglobbynotificationservice.dto.NotificationEventDto;
import com.raf.gaminglobbynotificationservice.dto.UserDto;
import com.raf.gaminglobbynotificationservice.model.Notification;
import com.raf.gaminglobbynotificationservice.model.NotificationStatus;
import com.raf.gaminglobbynotificationservice.repository.NotificationRepository;
import com.raf.gaminglobbynotificationservice.service.NotificationService;
import com.raf.gaminglobbynotificationservice.service.impl.EmailService;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import com.raf.jms.MessageHelper;
import org.springframework.web.client.RestTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.ServiceLoader;

@Component
public class NotificationListener {

    private MessageHelper messageHelper;
    private RestTemplate userServiceRestTemplate;
    private EmailService emailService;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;

    public NotificationListener(RestTemplate userServiceRestTemplate,
                                EmailService emailService,
                                NotificationService notificationService) {

        this.userServiceRestTemplate = userServiceRestTemplate;
        this.emailService = emailService;
        this.notificationService = notificationService;

        this.messageHelper =
                ServiceLoader.load(MessageHelper.class)
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("No MessageHelper implementation found"));
        this.notificationRepository = notificationRepository;
    }

    @JmsListener(destination = "${destination.notification}", concurrency = "1-5")
    public void onMessage(Message message) throws JMSException {

        NotificationEventDto event =
                messageHelper.getMessage(message, NotificationEventDto.class);


        Notification notification = notificationService.saveFromEvent(event);

        try {
            if ("SESSION_INVITE".equals(event.getType())) {

                UserDto user = userServiceRestTemplate.getForObject(
                        "/{id}",
                        UserDto.class,
                        event.getUserId()
                );

                if (user == null || user.getEmail() == null) {
                    throw new RuntimeException(
                            "User or email not found for id: " + event.getUserId()
                    );
                }

                String inviteLink =
                        "http://localhost:8080/sessions/join?token=" + event.getContent();

                emailService.sentEmail(user.getEmail(), inviteLink);

                notification.setStatus(NotificationStatus.SENT);
                notificationRepository.save(notification);
            }

            if ("USER_ACTIVATION".equals(event.getType())) {

                UserDto user = userServiceRestTemplate.getForObject(
                        "/{id}",
                        UserDto.class,
                        event.getUserId()
                );

                if (user == null || user.getEmail() == null) {
                    throw new RuntimeException("User or email not found for id: " + event.getUserId());
                }

                String activationLink =
                        "http://localhost:8081/user/activate?token=" + event.getContent();


                emailService.sentEmail(
                        user.getEmail(),
                        "Activate your acconut: \n" + activationLink
                );

                notificationService.markAsSent(notification.getId());
            }

            if ("SESSION_CANCELLED".equals(event.getType())) {

                UserDto user = userServiceRestTemplate.getForObject(
                        "/{id}",
                        UserDto.class,
                        event.getUserId()
                );

                emailService.sentEmail(user.getEmail(), event.getContent());

            }

            if (event.equals("REJECTED_SESSION")) {

            }

        } catch (Exception e) {
            System.err.println(
                    "FAILED TO PROCESS NOTIFICATION ID=" + notification.getId()
                            + " ERROR=" + e.getMessage()
            );
        }

        System.out.println("NOTIFICATION RECEIVED:");
        System.out.println("User ID: " + event.getUserId());
        System.out.println("Type: " + event.getType());
        System.out.println("Content: " + event.getContent());
    }

}

