package com.itmo.chgk.model.dto.request;

import com.itmo.chgk.model.dto.response.UserInfoResponse;
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
public class TeamInfoRequest {
    @NotEmpty
    String teamName;

    Long captainId;
    Long viceCaptainId;
}
