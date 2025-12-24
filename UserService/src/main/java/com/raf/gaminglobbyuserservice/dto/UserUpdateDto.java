package com.raf.gaminglobbyuserservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDto {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String password;
}
