package com.raf.gaminglobbyuserservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class UserCreateDto {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;


    public UserCreateDto() {}

    public UserCreateDto(String username, String password, String firstName, String lastName, LocalDate birthDate, String email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.email = email;
    }

}
