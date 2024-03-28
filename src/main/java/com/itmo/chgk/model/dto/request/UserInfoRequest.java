package com.itmo.chgk.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.mapping.List;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoRequest {
    @NotEmpty(message = "Email must be set")
    @Email(message = "Email should be valid")
    String email;

    @NotEmpty(message = "Login must be set")
    @Size(min = 4, message = "Логин слишком короткий")
    String login;

    @NotEmpty(message = "password must be set")
    @Size(min = 6, max = 20, message = "Пароль должен содержать от 6 до 20 символов")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{6,}$",
            message = "Пароль должен содержать, как минимум, 1 заглавную букву, 1 строчную букву и 1 цифру")
    String password;

    @Pattern(regexp = "^\\D*$", message = "Имя не должно содержать цифр")
    String firstName;
    @Pattern(regexp = "^\\D*$", message = "Фамилия не должна содержать цифр")
    String lastName;

    @Past()
    LocalDate birthDate;
}
