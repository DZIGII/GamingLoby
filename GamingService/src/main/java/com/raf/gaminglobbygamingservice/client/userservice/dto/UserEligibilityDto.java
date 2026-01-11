package com.raf.gaminglobbygamingservice.client.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEligibilityDto {
    private boolean blocked;
    private double attendancePercentage;
}

