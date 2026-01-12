package com.raf.gaminglobbygamingservice.repository;

import com.raf.gaminglobbygamingservice.model.Session;
import com.raf.gaminglobbygamingservice.model.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionParticipantRepository
        extends JpaRepository<SessionParticipant, Long> {

    boolean existsBySessionAndUserId(Session session, Long userId);
    List<SessionParticipant> findBySessionId(Long sessionId);
}
