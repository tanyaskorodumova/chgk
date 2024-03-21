package com.itmo.chgk.model.db.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "question_results")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    GameQuestion question;

    @OneToOne
    Team team;

    @Column(name = "is_correct")
    Boolean isCorrect;
}
