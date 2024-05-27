package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.AuthorityRequest;
import com.itmo.chgk.model.dto.request.URequest;
import com.itmo.chgk.model.dto.request.UserRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RestController
@RequestMapping("/users")
public class UserController {
    UserService userService;

    @GetMapping("/all")
    @Operation(summary = "Получение информации обо всех аккаунтах")
    public Page<UserResponse>  getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer perPage,
                                           @RequestParam(defaultValue = "username") String sort,
                                           @RequestParam(defaultValue = "ASC") Sort.Direction order){
            return userService.getAllUsers(page, perPage, sort, order);
        }

    @GetMapping("/{user}")
    @Operation(summary = "Получение информации об одном аккаунте")
    public UserResponse getUserResponse(@PathVariable String user){
            return userService.getUserResponse(user);
        }

    @PostMapping("/new")
    @Operation(summary = "Регистрация пользователя в программе")
    public JwtAuthenticationResponse createUser(@RequestBody @Valid URequest request){
        return userService.createUser(request);
    }

    @PutMapping("/password")
    @Operation(summary = "Изменение пароля пользователя")
    public String updatePassword(@RequestBody @Valid UserRequest request){
            return userService.updatePassword(request);
        }

    @PutMapping("/authority/set")
    @Operation(summary = "Изменение полномочий пользователя")
    public AuthorityResponse updateAuthority(@RequestBody AuthorityRequest request){
        return userService.updateAuthority(request);
    }

    @PutMapping("/authority/add")
    @Operation(summary = "Добавление полномочий пользователя")
    public AuthorityResponse addAuthority(@RequestBody AuthorityRequest request){
        return userService.addAuthority(request.getUsername(), request.getAuthorities());
    }

    @PutMapping("/authority/delete")
    @Operation(summary = "Отзыв полномочий пользователя")
    public AuthorityResponse deleteAuthority(@RequestBody AuthorityRequest request){
        return userService.deleteAuthority(request.getUsername(), request.getAuthorities());
    }

    @PutMapping("/{username}/{isEnable}")
    @Operation(summary = "Блокировка/разблокировка пользователя")
    public String setEnable(@PathVariable String username, @PathVariable boolean isEnable){
        return userService.setEnable(username, isEnable);
    }
}




