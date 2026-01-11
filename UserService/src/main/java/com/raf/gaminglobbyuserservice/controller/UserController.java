package com.raf.gaminglobbyuserservice.controller;


import com.raf.gaminglobbyuserservice.dto.*;
import com.raf.gaminglobbyuserservice.model.User;
import com.raf.gaminglobbyuserservice.model.VerificationToken;
import com.raf.gaminglobbyuserservice.repository.VerificationTokenRepository;
import com.raf.gaminglobbyuserservice.security.CheckSecurity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.raf.gaminglobbyuserservice.service.UserService;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5176")
@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private VerificationTokenRepository verificationTokenRepository;

    public UserController(UserService userService,  VerificationTokenRepository verificationTokenRepository) {
        this.userService = userService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @CheckSecurity(roles = {"ADMIN"})
    @GetMapping
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

    @CheckSecurity(roles = {"ADMIN"})
    @PostMapping("/unblock/{username}")
    public ResponseEntity<UserDto> unblockUser(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String username
    ) {
        return new ResponseEntity<>(userService.unblockUser(username), HttpStatus.OK);
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

    @GetMapping("/{id}")
    public ResponseEntity<Optional<UserDto>> getUserById(@PathVariable Long id) {
        return new ResponseEntity<>(userService.getUserByUId(id), HttpStatus.OK) ;
    }

    @GetMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam String token) {
        userService.activateUser(token);
        return ResponseEntity.ok().build();
    }

    //@CheckSecurity(roles = {"USER", "ADMIN"})
    @GetMapping("/eligibility/{id}")
    public ResponseEntity<UserEligibilityDto> checkEligibility(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.checkEligibility(id));
    }

    @CheckSecurity(roles = {"USER"})
    @PostMapping("/stats/{id}/joined")
    public ResponseEntity<Void> incrementJoined(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id
    ) {
        userService.incrementJoinedSessions(id);
        return ResponseEntity.ok().build();
    }


    @CheckSecurity(roles = {"USER"})
    @PostMapping("/stats/session-finished")
    public ResponseEntity<Void> sessionFinished(
            @RequestBody SessionFinishStatsDto dto
    ) {
        userService.processFinishedSession(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/internal/users/{id}/status")
    public UserStatsDto getUserStatus(@PathVariable Long id) {
        return userService.getUserStats(id);
    }


}
