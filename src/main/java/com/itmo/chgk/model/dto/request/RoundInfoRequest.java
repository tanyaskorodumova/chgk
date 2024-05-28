package com.itmo.chgk.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RoundInfoRequest {
    @NotEmpty(message = "Необходимо указать признак корректности ответа")
    Boolean isCorrect;
}
