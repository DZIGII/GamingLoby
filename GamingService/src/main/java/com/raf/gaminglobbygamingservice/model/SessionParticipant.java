package com.raf.gaminglobbygamingservice.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_participant_id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "user_id",  nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean attended;

    @Column(name = "left_early")
    private boolean leftEarly;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_sessions",  nullable = false)
    private RoleInSession roleInSession;

}
