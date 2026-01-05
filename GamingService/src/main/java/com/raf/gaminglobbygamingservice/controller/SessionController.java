package com.raf.gaminglobbygamingservice.controller;

import com.raf.gaminglobbygamingservice.dto.InvitationRequestDto;
import com.raf.gaminglobbygamingservice.dto.SessionDto;
import com.raf.gaminglobbygamingservice.model.SessionType;
import com.raf.gaminglobbygamingservice.security.CheckSecurity;
import com.raf.gaminglobbygamingservice.service.GamingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final GamingService gamingService;

    public SessionController(GamingService gamingService) {
        this.gamingService = gamingService;
    }

    @PostMapping
    @CheckSecurity(roles = {"USER"})
    public ResponseEntity<SessionDto> createSession(
            @RequestHeader("Authorization") String authorization,
            @RequestBody SessionDto sessionDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gamingService.createSession(authorization, sessionDto));
    }

    @GetMapping
    @CheckSecurity(roles = {"ADMIN", "USER"})
    public ResponseEntity<Page<SessionDto>> getSessions(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) SessionType sessionType,
            @RequestParam(required = false) Integer maxPlayers,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean joined,
            Pageable pageable,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                gamingService.serchSession(
                        authorization,
                        gameId,
                        sessionType,
                        maxPlayers,
                        description,
                        joined,
                        pageable
                )
        );
    }

    @CheckSecurity(roles = {"USER"})
    @PostMapping("/invitations")
    public ResponseEntity<Void> sendInvitation(
            @RequestHeader("Authorization") String authorization,
            @RequestBody InvitationRequestDto request
    ) {
        gamingService.sendInvite(authorization, request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/invitations/accept")
    public ResponseEntity<Void> acceptInvitation(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String token) {

        gamingService.acceptInvitation(authorization, token);
        return ResponseEntity.ok().build();
    }


}
