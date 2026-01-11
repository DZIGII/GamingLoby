package com.raf.gaminglobbygamingservice.client.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsDto {

    private String username;

    private int reportedSessions;
    private int attendedSessions;
    private int leftSessions;
    private double attendedPct;

    private String roleName;


}