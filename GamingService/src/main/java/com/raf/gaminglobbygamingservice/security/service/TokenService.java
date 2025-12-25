package com.raf.gaminglobbygamingservice.security.service;

import io.jsonwebtoken.Claims;

public interface TokenService {

    String generate(Claims claims);

    Claims parse(String token);

}
