package com.raf.gaminglobbyuserservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequestDto {

    private String username;
    private String password;

    public TokenRequestDto() {}

    public TokenRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
