package com.raf.gaminglobbygamingservice.security;

import com.raf.gaminglobbygamingservice.security.service.TokenService;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Configuration
public class SecurityAspect {

    private final HttpServletRequest request;
    private final TokenService tokenService;

    public SecurityAspect(HttpServletRequest request, TokenService tokenService) {
        this.request = request;
        this.tokenService = tokenService;
    }

    @Around("@annotation(com.raf.gaminglobbygamingservice.security.CheckSecurity)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }

        String token = authHeader.substring(7);

        Claims claims;
        try {
            claims = tokenService.parse(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckSecurity checkSecurity = method.getAnnotation(CheckSecurity.class);

        String role = claims.get("role", String.class);

        if (!Arrays.asList(checkSecurity.roles()).contains(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        return joinPoint.proceed();
    }

}

