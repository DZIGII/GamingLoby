package com.raf.gaminglobbyuserservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_stats_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "title_id")
    private Title title;

    @Column(name = "reporte_sessions",   nullable = false)
    private int reporteSessions;

    @Column(name = "attended_sessions",   nullable = false)
    private int attendedSessions;

    @Column(name = "left_sessions",   nullable = false)
    private int leftSessions;

    @Column(name = "attended_pct",   nullable = false)
    private int attendedPct = 100;

    public  UserStats() {}

    public UserStats(User user, Title title, int reporteSessions, int attendedSessions, int leftSessions, int attendedPct) {
        this.user = user;
        this.title = title;
        this.reporteSessions = reporteSessions;
        this.attendedSessions = attendedSessions;
        this.leftSessions = leftSessions;
        this.attendedPct = attendedPct;
    }

    @Override
    public String toString() {
        return "UserStats{" +
                "id=" + id +
                ", user=" + user +
                ", title=" + title +
                ", reporteSessions=" + reporteSessions +
                ", attendedSessions=" + attendedSessions +
                ", leftSessions=" + leftSessions +
                ", attendedPct=" + attendedPct +
                '}';
    }
}
