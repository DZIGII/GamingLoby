package com.raf.gaminglobbygamingservice.mapper;

import com.raf.gaminglobbygamingservice.dto.GameDto;
import com.raf.gaminglobbygamingservice.model.Game;
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
        gameDto.setName(game.getName());
        gameDto.setDescription(game.getDescription());
        gameDto.setGenre(game.getGener());

        return gameDto;
    }

}
