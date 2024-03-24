package com.itmo.chgk.model.dto.request;

import com.itmo.chgk.model.enums.TournamentLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentInfoRequest {
    String tournName;
    TournamentLevel level;
    Integer tournFactor;
    Integer minPoints;
}
