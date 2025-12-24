package com.raf.gaminglobbygamingservice.service;

import com.raf.gaminglobbygamingservice.dto.GameDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GamingService {

    void createGame(String token, GameDto game);

    Page<GameDto> getGames(Pageable pageable);

    GameDto updateGame(String token, Long id, GameDto game);
}
