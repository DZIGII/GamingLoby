package com.raf.gaminglobbygamingservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationRequestDto {

    private Long sessionId;
    private Long invitedUserId;

}
