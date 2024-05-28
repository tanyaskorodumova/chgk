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
@Table(name = "tournament_table",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "team_id"})
)
public class TournamentTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Tournament tournament;

    @ManyToOne
    Team team;

    Integer points;
}
