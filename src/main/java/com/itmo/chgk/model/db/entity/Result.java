package com.itmo.chgk.model.db.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "team_id"})
)
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Game game;

    @ManyToOne
    Team team;

    Integer points;

    Integer place;
}
