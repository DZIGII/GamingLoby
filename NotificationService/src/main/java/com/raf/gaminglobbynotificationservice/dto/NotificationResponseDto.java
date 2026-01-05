package com.raf.gaminglobbynotificationservice.dto;


import com.raf.gaminglobbynotificationservice.model.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {

    private Long id;
    private String type;
    private String content;
    private NotificationStatus status;
    private LocalDateTime sentAt;

}
