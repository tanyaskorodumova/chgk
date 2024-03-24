package com.itmo.chgk.model.dto.request;

import com.itmo.chgk.model.enums.Stage;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameInfoRequest {
    String gameName;

    @Future
    LocalDateTime dateTime;

    String place;
    Long tournamentId;
    Stage stage;

}
