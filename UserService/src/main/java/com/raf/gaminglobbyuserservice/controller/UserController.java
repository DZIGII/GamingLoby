package com.raf.gaminglobbyuserservice.controller;


import com.raf.gaminglobbyuserservice.dto.*;
import com.raf.gaminglobbyuserservice.repository.VerificationTokenRepository;
import com.raf.gaminglobbyuserservice.security.CheckSecurity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.raf.gaminglobbyuserservice.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private VerificationTokenRepository verificationTokenRepository;

    public UserController(UserService userService,  VerificationTokenRepository verificationTokenRepository) {
        this.userService = userService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @GetMapping
    @CheckSecurity(roles = {"ADMIN"})
    public ResponseEntity<Page<UserDto>> getAllUser(
            @RequestHeader("Authorization") String authorization,
            Pageable pageable) {
        return new ResponseEntity<>(userService.findAll(pageable), HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> saveUser(@RequestBody UserCreateDto userCreateDto) {
        return new ResponseEntity<>(userService.register(userCreateDto),  HttpStatus.CREATED);
    }

    @CheckSecurity(roles = {"USER"})
    @PostMapping("/update")
    public ResponseEntity<UserDto> updateUser(
            @RequestHeader("Authorization") String authorization,
            @RequestBody UserUpdateDto userUpdateDto) {

        return new ResponseEntity<>(
                userService.updateProfileData(authorization, userUpdateDto),
                HttpStatus.OK
        );
    }

    @CheckSecurity(roles = {"ADMIN"})
    @PostMapping("/block/{username}")
    public ResponseEntity<UserDto> blockUser(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String username
    ) {
        return new ResponseEntity<>(userService.blockUser(username), HttpStatus.OK);
    }



    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginUser(@RequestBody TokenRequestDto tokenRequestDto) {
        return new ResponseEntity<>(userService.login(tokenRequestDto), HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        userService.verifyUser(token);
        return ResponseEntity.ok("Account verified");
    }


}
