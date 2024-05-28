package com.itmo.chgk.model.db.entity;

import com.itmo.chgk.model.enums.ParticipantStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "game_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "participant_id"})
)
public class GameParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Game game;

    @ManyToOne
    Team participant;

    ParticipantStatus status;
}
