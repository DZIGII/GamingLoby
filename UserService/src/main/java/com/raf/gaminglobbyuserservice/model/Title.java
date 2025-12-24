package com.raf.gaminglobbyuserservice.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "titles")
public class Title {

    @Id
    @GeneratedValue
    @Column(name = "title_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "title_name", nullable = false)
    private TitleName name;
    private int minSessions;

    protected Title() {}

    public Title(TitleName name, int minSessions) {
        this.name = name;
        this.minSessions = minSessions;
    }
}
