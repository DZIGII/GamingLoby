package com.raf.gaminglobbyuserservice.dto;

import com.raf.gaminglobbyuserservice.model.Title;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserStatsDto {

    private String username;

    private int reportedSessions;
    private int attendedSessions;
    private int leftSessions;
    private double attendedPct;

    private String roleName;

    private Title title;


    public UserStatsDto() {}

    public UserStatsDto(String username, int reportedSessions, int attendedSessions, int leftSessions, double attendedPct, String roleName,  Title title) {
        this.username = username;
        this.reportedSessions = reportedSessions;
        this.attendedSessions = attendedSessions;
        this.leftSessions = leftSessions;
        this.attendedPct = attendedPct;
        this.roleName = roleName;
        this.title = title;
    }
}
