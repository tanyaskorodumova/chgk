package com.itmo.chgk.model.dto.response;

import com.itmo.chgk.model.db.entity.Authority;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    private String username;
    private String token;
    private boolean enabled;
    private List<Authority> authorities = new ArrayList<Authority>();
}
