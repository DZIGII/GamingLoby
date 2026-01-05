package com.raf.gaminglobbynotificationservice.service;

import com.raf.gaminglobbynotificationservice.dto.NotificationEventDto;
import com.raf.gaminglobbynotificationservice.dto.NotificationResponseDto;
import com.raf.gaminglobbynotificationservice.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Notification saveFromEvent(NotificationEventDto event);
    void markAsSent(Long notificationId);
    void processNotification(NotificationEventDto notification);
    Page<NotificationResponseDto> getNotifications(Pageable pageable);
}
