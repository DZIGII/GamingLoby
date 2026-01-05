package com.raf.gaminglobbynotificationservice.security.service;

import io.jsonwebtoken.Claims;

public interface TokenService {

    String generate(Claims claims);

    Claims parse(String token);

}
