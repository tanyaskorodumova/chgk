package com.itmo.chgk.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundInfoRequest {
    @NotEmpty(message = "Необходимо указать признак корректности ответа")
    Boolean isCorrect;
}
