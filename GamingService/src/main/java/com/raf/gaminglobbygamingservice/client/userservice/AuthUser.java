package com.raf.gaminglobbygamingservice.client.userservice;

import jakarta.persistence.Column;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthUser {

    private RestTemplate userServiceRestTemplate;

    public AuthUser(RestTemplate userServiceRestTemplate) {
        this.userServiceRestTemplate = userServiceRestTemplate;
    }

    public AuthUserDto getCurrentUser(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return userServiceRestTemplate.exchange("/auth/me", HttpMethod.GET, entity, AuthUserDto.class).getBody();
    }

}
