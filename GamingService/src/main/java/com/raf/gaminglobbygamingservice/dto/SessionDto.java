package com.raf.gaminglobbygamingservice.dto;

import com.raf.gaminglobbygamingservice.model.SessionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SessionDto {

    private Long id;
    private String name;
    private Long gameId;
    private int maxPlayers;
    private SessionType sessionType;
    private LocalDateTime startTime;
    private String description;
}
