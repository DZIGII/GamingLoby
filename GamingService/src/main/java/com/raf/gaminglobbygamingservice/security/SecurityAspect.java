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

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Configuration
public class SecurityAspect {

    private final TokenService tokenService;

    public SecurityAspect(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Around("@annotation(com.raf.gaminglobbygamingservice.security.CheckSecurity)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String token = null;

        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            if ("authorization".equals(paramNames[i])) {
                String authHeader = args[i].toString();
                if (authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
        }

        if (token == null) {
            throw new RuntimeException("Missing Authorization header");
        }

        Claims claims = tokenService.parse(token);

        if (claims == null) {
            throw new RuntimeException("Invalid token");
        }

        CheckSecurity checkSecurity = method.getAnnotation(CheckSecurity.class);
        String role = claims.get("role", String.class);

        if (!Arrays.asList(checkSecurity.roles()).contains(role)) {
            throw new RuntimeException("Forbidden");
        }

        return joinPoint.proceed();
    }
}

