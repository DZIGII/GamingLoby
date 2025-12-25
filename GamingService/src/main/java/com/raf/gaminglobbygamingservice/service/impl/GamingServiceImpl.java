package com.raf.gaminglobbygamingservice.service.impl;

import com.raf.gaminglobbygamingservice.client.userservice.dto.AuthUserDto;
import com.raf.gaminglobbygamingservice.dto.GameDto;
import com.raf.gaminglobbygamingservice.dto.SessionDto;
import com.raf.gaminglobbygamingservice.mapper.GamingMapper;
import com.raf.gaminglobbygamingservice.model.Game;
import com.raf.gaminglobbygamingservice.model.Session;
import com.raf.gaminglobbygamingservice.model.SessionStatus;
import com.raf.gaminglobbygamingservice.repository.GamingRepository;
import com.raf.gaminglobbygamingservice.repository.SessionRepository;
import com.raf.gaminglobbygamingservice.security.service.TokenService;
import com.raf.gaminglobbygamingservice.service.GamingService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GamingServiceImpl implements GamingService {

    private SessionRepository sessionRepository;
    private GamingRepository gamingRepository;
    private GamingMapper gamingMapper;
    private TokenService tokenService;



    public GamingServiceImpl(SessionRepository sessionRepository, GamingRepository gamingRepository, GamingMapper gamingMapper, TokenService tokenService) {
        this.sessionRepository = sessionRepository;
        this.gamingRepository = gamingRepository;
        this.gamingMapper = gamingMapper;
        this.tokenService = tokenService;
    }

    @Override
    public void createGame(String token, GameDto game) {

        Game g = gamingRepository.findGameByName(game.getName());
        if (g != null) throw new RuntimeException("Game already exists");

        gamingRepository.save(gamingMapper.GameDtoToGame(game));

    }

    @Override
    public Page<GameDto> getGames(Pageable pageable) {
        return gamingRepository.findAll(pageable)
                .map(gamingMapper::gameToGameDto);
    }

    @Override
    public GameDto updateGame(String token, Long id, GameDto game) {

        Game gg = gamingRepository.findGameByName(game.getName());

        if (gg != null) throw new RuntimeException("Game already exists");

        Game g = gamingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        g.setName(game.getName());
        g.setDescription(game.getDescription());
        g.setGener(game.getGenre());

        gamingRepository.save(g);

        return gamingMapper.gameToGameDto(g);
    }

    @Override
    public SessionDto createSession(String token, SessionDto sessionDto) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);

        String role = claims.get("role", String.class);
        Long userId = claims.get("userId", Long.class);

        if (!role.equals("USER")) {
            throw new RuntimeException("Only USER can create sessions");
        }

        if (sessionDto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Session start time must be in the future");
        }

        Session session = gamingMapper.sessionDtoToSession(sessionDto);

        session.setOrganizer(userId);
        session.setStatus(SessionStatus.SCHEDULED);

        Game game = gamingRepository.findById(sessionDto.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        session.setGame(game);

        Session saved = sessionRepository.save(session);

        return gamingMapper.sessionToDto(saved);
    }



}
