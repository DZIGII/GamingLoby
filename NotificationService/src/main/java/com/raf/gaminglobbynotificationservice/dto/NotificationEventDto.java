package com.raf.gaminglobbynotificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEventDto {

    private Long userId;

    private String type;

    private String content;
}
