package com.itmo.chgk.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itmo.chgk.model.dto.request.RoundInfoRequest;
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
public class RoundInfoResponse extends RoundInfoRequest {
    Long gameId;
    Integer round;
    Long questionId;
    String question;
    Long teamId;
    String teamName;
}
