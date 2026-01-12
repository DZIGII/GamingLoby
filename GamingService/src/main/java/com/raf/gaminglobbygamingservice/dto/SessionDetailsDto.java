package com.raf.gaminglobbygamingservice.dto;

import com.raf.gaminglobbygamingservice.model.SessionStatus;
import com.raf.gaminglobbygamingservice.model.SessionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SessionDetailsDto {

    private Long id;
    private String name;
    private SessionType sessionType;
    private SessionStatus status;
    private LocalDateTime startTime;

    private List<SessionParticipantViewDto> participants;
}
