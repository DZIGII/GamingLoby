package com.raf.gaminglobbygamingservice.repository;

import com.raf.gaminglobbygamingservice.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);

    @Query("""
        select i.invitedUserId
        from Invitation i
        where i.session.id = :sessionId
    """)
    List<Long> findInvitedUserIdsBySessionId(Long sessionId);
}
