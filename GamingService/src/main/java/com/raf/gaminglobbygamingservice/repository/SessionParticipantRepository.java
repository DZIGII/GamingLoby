package com.raf.gaminglobbygamingservice.repository;

import com.raf.gaminglobbygamingservice.model.Session;
import com.raf.gaminglobbygamingservice.model.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionParticipantRepository
        extends JpaRepository<SessionParticipant, Long> {

    boolean existsBySessionAndUserId(Session session, Long userId);

}
