package com.raf.gaminglobbygamingservice.service.impl;

import com.raf.gaminglobbygamingservice.client.userservice.AuthUser;
import com.raf.gaminglobbygamingservice.client.userservice.AuthUserDto;
import com.raf.gaminglobbygamingservice.dto.GameDto;
import com.raf.gaminglobbygamingservice.mapper.GamingMapper;
import com.raf.gaminglobbygamingservice.model.Game;
import com.raf.gaminglobbygamingservice.repository.GamingRepository;
import com.raf.gaminglobbygamingservice.service.GamingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GamingServiceImpl implements GamingService {

    private GamingRepository gamingRepository;
    private AuthUser authUser;
    private GamingMapper gamingMapper;

    public GamingServiceImpl(GamingRepository gamingRepository, AuthUser authUser, GamingMapper gamingMapper) {
        this.gamingRepository = gamingRepository;
        this.authUser = authUser;
        this.gamingMapper = gamingMapper;
    }

    @Override
    public void createGame(String token, GameDto game) {

        AuthUserDto authUserDto = authUser.getCurrentUser(token);

        if (!authUserDto.getRole().equals("ADMIN")) {
            throw new RuntimeException("You are not allowed to perform this operation");
        }


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

        AuthUserDto authUserDto = authUser.getCurrentUser(token);

        if (!authUserDto.getRole().equals("ADMIN")) {
            throw new RuntimeException("You are not allowed to perform this operation");
        }

        Game g = gamingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        g.setName(game.getName());
        g.setDescription(game.getDescription());
        g.setGener(game.getGenre());

        gamingRepository.save(g);

        return gamingMapper.gameToGameDto(g);
    }

}
