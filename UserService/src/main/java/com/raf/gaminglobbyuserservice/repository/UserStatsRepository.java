package com.raf.gaminglobbyuserservice.repository;

import com.raf.gaminglobbyuserservice.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    Optional<UserStats> findByUser_Id(Long id);
}
