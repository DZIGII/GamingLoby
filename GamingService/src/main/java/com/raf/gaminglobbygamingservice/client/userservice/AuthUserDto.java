package com.raf.gaminglobbygamingservice.client.userservice;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserDto {

    private Long userId;
    private String role;
    private boolean blocked;
    private double attendancePercentage;

}
