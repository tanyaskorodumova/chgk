package com.itmo.chgk.model.db.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "game_questions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    Game game;

    @OneToOne
    Question question;

    Integer round;
}
