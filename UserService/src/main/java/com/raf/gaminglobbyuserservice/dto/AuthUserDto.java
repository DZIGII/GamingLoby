package com.raf.gaminglobbyuserservice.dto;

import com.raf.gaminglobbyuserservice.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserDto {

    private Long userId;
    private String role;
    private boolean blocked;
    private Double attendancePercentage;

}
