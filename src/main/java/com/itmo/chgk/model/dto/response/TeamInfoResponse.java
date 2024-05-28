package com.itmo.chgk.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamInfoResponse {
    Long id;
    String teamName;
    UserInfoResponse captain;
    UserInfoResponse viceCaptain;
    Integer points;
    Integer correctAnswers;
    Double correctAnswersPct;
}
