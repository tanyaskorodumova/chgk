package com.itmo.chgk.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorityRequest {
    @NotEmpty(message = "Login must be set")
    @Size(min = 4, message = "Логин слишком короткий")
    String username;

    List<String> authorities = new ArrayList<String>();
}
