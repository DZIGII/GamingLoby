package com.raf.gaminglobbynotificationservice.controller;

import com.raf.gaminglobbynotificationservice.dto.NotificationResponseDto;
import com.raf.gaminglobbynotificationservice.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notifications")
public class NotificationControllerAdmin {

    private NotificationService notificationService;

    public NotificationControllerAdmin(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //@CheckSecurity(roles = {"ADMIN"})
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getNotifications(
            //@RequestHeader("Authorization") String authorization,
            Pageable pageable) {
        return new ResponseEntity<>(notificationService.getNotifications(pageable), HttpStatus.OK);
    }
}
