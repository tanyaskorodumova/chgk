package com.itmo.chgk.model.db.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "tournament_table")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    Tournament tournament;

    @OneToOne
    Team team;

    Integer points;
}
