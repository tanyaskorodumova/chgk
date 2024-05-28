package com.itmo.chgk.model.dto.request;

import com.itmo.chgk.model.enums.QuestionComplexity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionPackRequest {
    Integer number;
    QuestionComplexity minComplexity;
    QuestionComplexity maxComplexity;
}
