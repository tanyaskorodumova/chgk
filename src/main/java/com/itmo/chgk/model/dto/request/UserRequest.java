package com.itmo.chgk.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotEmpty(message = "Login must be set")
    @Size(min = 4, message = "Логин слишком короткий")
    String username;

    @NotEmpty(message = "password must be set")
    @Size(min = 6, max = 20, message = "Пароль должен содержать от 6 до 20 символов")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{6,}$",
            message = "Пароль должен содержать, как минимум, 1 заглавную букву, 1 строчную букву и 1 цифру")
    String password;
}
