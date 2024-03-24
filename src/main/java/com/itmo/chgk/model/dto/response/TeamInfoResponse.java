package com.itmo.chgk.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamInfoResponse extends TeamInfoRequest {
    Long id;
    UserInfoResponse captain;
    UserInfoResponse viceCaptain;
    Integer points;
    Integer correctAnswers;
    Double correctAnswersPct;
}
