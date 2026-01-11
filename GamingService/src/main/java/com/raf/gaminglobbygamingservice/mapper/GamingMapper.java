package com.raf.gaminglobbygamingservice.mapper;

import com.raf.gaminglobbygamingservice.dto.GameDto;
import com.raf.gaminglobbygamingservice.dto.SessionDto;
import com.raf.gaminglobbygamingservice.model.Game;
import com.raf.gaminglobbygamingservice.model.Session;
import com.raf.gaminglobbygamingservice.model.SessionType;
import org.springframework.stereotype.Component;

@Component
public class GamingMapper {

    public Game GameDtoToGame(GameDto gameDto) {
        Game game = new Game();
        game.setName(gameDto.getName());
        game.setDescription(gameDto.getDescription());
        game.setGener(gameDto.getGenre());

        return game;
    }

    public GameDto gameToGameDto(Game game) {
        GameDto gameDto = new GameDto();
        gameDto.setId(game.getId());
        gameDto.setName(game.getName());
        gameDto.setDescription(game.getDescription());
        gameDto.setGenre(game.getGener());

        return gameDto;
    }

    public Session sessionDtoToSession(SessionDto dto) {

        Session session = new Session();
        session.setId(dto.getId());
        session.setName(dto.getName());
        session.setDescription(dto.getDescription());
        session.setMaxPlayers(dto.getMaxPlayers());
        session.setStartTime(dto.getStartTime());
        session.setSessionType(dto.getSessionType());

        return session;
    }

    public SessionDto sessionToDto(Session session) {

        SessionDto dto = new SessionDto();
        dto.setId(session.getId());
        dto.setName(session.getName());
        dto.setDescription(session.getDescription());
        dto.setMaxPlayers(session.getMaxPlayers());
        dto.setSessionType(session.getSessionType());
        dto.setStartTime(session.getStartTime());
        dto.setGameId(session.getGame().getId());

        return dto;
    }



}
