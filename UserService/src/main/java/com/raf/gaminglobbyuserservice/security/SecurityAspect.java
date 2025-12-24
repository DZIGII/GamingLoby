package com.raf.gaminglobbyuserservice.security;

import com.raf.gaminglobbyuserservice.security.service.TokenService;
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

    @Around("@annotation(com.raf.gaminglobbyuserservice.security.CheckSecurity)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        System.out.println("=== SECURITY ASPECT HIT ===");


        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String token = null;

        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            System.out.println("Param name = " + paramNames[i]);
            System.out.println("Arg value = " + args[i]);

            if ("authorization".equals(paramNames[i])) {
                String authHeader = args[i].toString();
                System.out.println("AUTH HEADER FOUND = " + authHeader);

                if (authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    System.out.println("TOKEN EXTRACTED = " + token);
                } else {
                    System.out.println("AUTH HEADER DOES NOT START WITH Bearer");
                }
            }
        }

        System.out.println("FINAL TOKEN = " + token);



        if (token == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Claims claims = tokenService.parseToken(token);

        System.out.println("CLAIMS FROM TOKEN = " + claims);

        if (claims == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CheckSecurity checkSecurity = method.getAnnotation(CheckSecurity.class);
        String role = claims.get("role", String.class);

        System.out.println("ROLE FROM TOKEN = " + role);
        System.out.println("ALLOWED ROLES = " + Arrays.toString(checkSecurity.roles()));


        if (Arrays.asList(checkSecurity.roles()).contains(role)) {
            return joinPoint.proceed();
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
