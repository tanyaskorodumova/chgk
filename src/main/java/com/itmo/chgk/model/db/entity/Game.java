package com.itmo.chgk.model.db.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.itmo.chgk.model.enums.GameStatus;
import com.itmo.chgk.model.enums.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "games")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "game_name")
    String gameName;

    @Column(name = "date_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime dateTime;

    String place;

    @ManyToOne
    @JsonManagedReference(value = "tournament_games")
    Tournament tournament;

    Stage stage;

    @OneToMany
    @JsonManagedReference(value = "game_teams")
    List<Team> participants;

    GameStatus status;

    @Column(name = "created_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime updatedAt;
}
