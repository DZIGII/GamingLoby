package com.raf.gaminglobbygamingservice.client.userservice;

import com.raf.gaminglobbygamingservice.client.userservice.dto.UserDto;
import com.raf.gaminglobbygamingservice.client.userservice.dto.UserEligibilityDto;
import com.raf.gaminglobbygamingservice.client.userservice.dto.UserStatsDto;
import com.raf.gaminglobbygamingservice.dto.SessionFinishStatsDto;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserServiceClient {

    private final RestTemplate userServiceRestTemplate;

    public UserServiceClient(@Qualifier("userServiceRestTemplate") RestTemplate userServiceRestTemplate) {
        this.userServiceRestTemplate = userServiceRestTemplate;
    }

    private HttpEntity<Void> authEntity(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        return new HttpEntity<>(headers);
    }

    private <T> HttpEntity<T> authEntity(String authorization, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        return new HttpEntity<>(body, headers);
    }

    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public UserEligibilityDto checkEligibility(String authorization, Long userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);

        HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        ResponseEntity<UserEligibilityDto> response =
                userServiceRestTemplate.exchange(
                        "/eligibility/{id}",
                        HttpMethod.GET,
                        entity,
                        UserEligibilityDto.class,
                        userId
                );

        return response.getBody();
    }



    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void incrementJoined(String authorization, Long userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);

        HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        userServiceRestTemplate.exchange(
                "/stats/{id}/joined",
                HttpMethod.POST,
                entity,
                Void.class,
                userId
        );
    }


    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void notifySessionFinished(String authorization, SessionFinishStatsDto dto) {
        userServiceRestTemplate.exchange(
                "/stats/session-finished",
                HttpMethod.POST,
                authEntity(authorization, dto),
                Void.class
        );
    }

    public UserStatsDto getUserStats(Long userId, String authorization) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserStatsDto> response =
                userServiceRestTemplate.exchange(
                        "/internal/users/{id}/status",
                        HttpMethod.GET,
                        entity,
                        UserStatsDto.class,
                        userId
                );

        return response.getBody();
    }

    public Map<Long, String> getUsernamesMap(
            List<Long> userIds,
            String authorization
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);

        HttpEntity<List<Long>> entity =
                new HttpEntity<>(userIds, headers);

        ResponseEntity<UserDto[]> response =
                userServiceRestTemplate.exchange(
                        "/internal/users/batch",
                        HttpMethod.POST,
                        entity,
                        UserDto[].class
                );

        return Arrays.stream(response.getBody())
                .collect(Collectors.toMap(
                        UserDto::getId,
                        UserDto::getUsername
                ));
    }

}

