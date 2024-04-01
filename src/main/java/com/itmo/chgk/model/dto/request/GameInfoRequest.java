package com.itmo.chgk.model.dto.request;

import com.itmo.chgk.model.enums.Stage;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
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

    @Min(value = 1, message = "Максимальное количество участников не может быть меньше 1")
    Integer maxParticipants;

}
