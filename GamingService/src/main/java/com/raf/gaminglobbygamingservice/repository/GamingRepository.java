package com.raf.gaminglobbygamingservice.repository;

import com.raf.gaminglobbygamingservice.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GamingRepository extends JpaRepository<Game, Long> {
    Page<Game> findAll(Pageable pageable);
    Game findGameByName(String name);
    Optional<Game> findGameById(Long id);

}
