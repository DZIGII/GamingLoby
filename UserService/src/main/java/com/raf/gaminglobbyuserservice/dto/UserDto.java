package com.raf.gaminglobbyuserservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class UserDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private Boolean blocked;

    public UserDto() {}
}
