package com.itmo.chgk.model.db.entity;

import com.itmo.chgk.model.enums.ParticipantStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "game_participants")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    Game game;

    @OneToOne
    Team participant;

    ParticipantStatus status;
}
