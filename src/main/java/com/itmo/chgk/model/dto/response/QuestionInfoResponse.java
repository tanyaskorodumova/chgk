package com.itmo.chgk.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.enums.QuestionComplexity;
import com.itmo.chgk.model.enums.QuestionStatus;
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
public class QuestionInfoResponse extends QuestionInfoRequest{
    Long id;
    QuestionStatus status;
}
