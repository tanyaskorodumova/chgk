package com.itmo.chgk.model.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.itmo.chgk.model.enums.CommonStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "teams")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "team_name", unique = true)
    String teamName;

    @OneToOne
    UserInfo captain;

    @OneToOne
    UserInfo viceCaptain;

    @JsonManagedReference(value = "team_users")
    @OneToMany(mappedBy = "team")
    List<UserInfo> userInfos;

    Integer points;

    Integer answers;

    @Column(name = "correct_answers")
    Integer correctAnswers;

    @Column(name = "correct_answers_pct")
    Double correctAnswersPct;

    CommonStatus status;

    @Column(name = "created_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime updatedAt;
}
