package com.raf.gaminglobbygamingservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SessionFinishStatsDto {
    private Long sessionId;
    private Long organizerId;
    private List<Long> attendedUserIds;
    private List<Long> absentUserIds;
}

