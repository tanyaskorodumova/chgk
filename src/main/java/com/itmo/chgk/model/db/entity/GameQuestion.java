package com.itmo.chgk.model.db.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "game_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "question_id"})
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Game game;

    @ManyToOne
    Question question;

    Integer round;
}
