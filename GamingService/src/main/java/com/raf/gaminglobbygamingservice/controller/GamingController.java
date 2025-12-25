package com.raf.gaminglobbygamingservice.controller;

import com.raf.gaminglobbygamingservice.dto.GameDto;
import com.raf.gaminglobbygamingservice.dto.SessionDto;
import com.raf.gaminglobbygamingservice.model.Session;
import com.raf.gaminglobbygamingservice.security.CheckSecurity;
import com.raf.gaminglobbygamingservice.service.GamingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GamingController {

    private GamingService gamingService;

    public GamingController(GamingService gamingService) {
        this.gamingService = gamingService;
    }

    @CheckSecurity(roles = {"ADMIN"})
    @PostMapping
    public ResponseEntity<Void> addGame(
            @RequestHeader("Authorization") String authorization,
            @RequestBody GameDto gameDto) {

        gamingService.createGame(authorization, gameDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<GameDto>> getGames(Pageable pageable) {
        return new ResponseEntity<>(gamingService.getGames(pageable), HttpStatus.ACCEPTED);
    }

    @CheckSecurity(roles = {"ADMIN"})
    @PutMapping("/{id}")
    public GameDto updateGame(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody GameDto gameDto) {

        return gamingService.updateGame(authorization, id, gameDto);
    }

    @PostMapping("/session")
    @CheckSecurity(roles = {"USER"})
    public ResponseEntity<SessionDto> createSession(
            @RequestHeader("Authorization") String authorization,
            @RequestBody SessionDto sessionDto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gamingService.createSession(authorization, sessionDto));
    }


}
