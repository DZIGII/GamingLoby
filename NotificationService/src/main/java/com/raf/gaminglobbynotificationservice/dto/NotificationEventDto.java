package com.raf.gaminglobbynotificationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEventDto {

    @NotNull
    private Long userId;

    @NotNull
    private String type;

    private String content;
}
