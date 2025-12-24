package com.raf.gaminglobbyuserservice.security.service;

import io.jsonwebtoken.Claims;

public interface TokenService {

    String generate(Claims claims);

    Claims parseToken(String jwt);

    String extractUsername(String authorization);
}
