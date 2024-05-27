package com.itmo.chgk.model.dto.request;


import com.itmo.chgk.model.db.entity.Authority;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorityRequest {
    @NotEmpty(message = "Login must be set")
    @Size(min = 4, message = "Логин слишком короткий")
    String username;

    List<String> authorities = new ArrayList<String>();
}
