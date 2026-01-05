package com.raf.gaminglobbygamingservice.service;

import com.raf.gaminglobbygamingservice.dto.GameDto;
import com.raf.gaminglobbygamingservice.dto.InvitationRequestDto;
import com.raf.gaminglobbygamingservice.dto.SessionDto;
import com.raf.gaminglobbygamingservice.model.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GamingService {

    void createGame(String token, GameDto game);

    Page<GameDto> getGames(Pageable pageable);

    GameDto updateGame(String token, Long id, GameDto game);

    SessionDto createSession(String token, SessionDto session);

    Page<SessionDto> serchSession(
            String authorization,
            Long gameId,
            SessionType sessionType,
            Integer maxPlayers,
            String description,
            Boolean joined,
            Pageable pageable
    );

    Void sendInvite(String token, InvitationRequestDto invitationRequestDto);

    void acceptInvitation(String token, String invitationToken);

    Void cancleSession(String token, Long sessionId);

}
