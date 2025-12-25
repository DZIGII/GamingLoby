package com.raf.gaminglobbygamingservice.client.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class UserDto {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;

    public UserDto() {}
}
