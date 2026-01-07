package com.raf.gaminglobbygamingservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionParticipantDto {

    private Long sessionId;
    private Long userId;
    private String roleInSession;

}

