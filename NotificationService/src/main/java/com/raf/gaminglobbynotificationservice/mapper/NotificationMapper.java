package com.raf.gaminglobbynotificationservice.mapper;

import com.raf.gaminglobbynotificationservice.dto.NotificationResponseDto;
import com.raf.gaminglobbynotificationservice.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDto notificationToNotificationDto(Notification notification) {
        NotificationResponseDto notificationResponseDto = new NotificationResponseDto();

        notificationResponseDto.setId(notification.getUserId());
        notificationResponseDto.setContent(notification.getContent());
        notificationResponseDto.setType(notification.getType());
        notificationResponseDto.setStatus(notification.getStatus());
        notificationResponseDto.setSentAt(notification.getSentAt());

        return notificationResponseDto;
    }
}
