package com.itmo.chgk.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itmo.chgk.model.enums.ParticipantStatus;
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
public class ParticipantsInfoResponse extends TeamInfoResponse{
    Long gameId;
    ParticipantStatus participantStatus;
}
