package com.raf.gaminglobbynotificationservice.service.impl;

import com.raf.gaminglobbynotificationservice.dto.NotificationEventDto;
import com.raf.gaminglobbynotificationservice.dto.NotificationResponseDto;
import com.raf.gaminglobbynotificationservice.mapper.NotificationMapper;
import com.raf.gaminglobbynotificationservice.model.Notification;
import com.raf.gaminglobbynotificationservice.model.NotificationStatus;
import com.raf.gaminglobbynotificationservice.repository.NotificationRepository;
import com.raf.gaminglobbynotificationservice.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private NotificationRepository notificationRepository;
    private NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public Notification saveFromEvent(NotificationEventDto event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(event.getType());
        notification.setContent(event.getContent());
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAsSent(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow();
        notification.setStatus(NotificationStatus.SENT);
    }

    @Override
    public void processNotification(NotificationEventDto notification) {

    }

    @Override
    public Page<NotificationResponseDto> getNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(notificationMapper::notificationToNotificationDto);
    }
}
