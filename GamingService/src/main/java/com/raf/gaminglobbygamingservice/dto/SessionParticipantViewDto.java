package com.raf.gaminglobbygamingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessionParticipantViewDto {

    private Long userId;
    private String username;
    private String roleInSession;
}
