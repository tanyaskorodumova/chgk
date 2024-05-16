package com.itmo.chgk.controllers;

import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.dto.request.UserInfoRequest;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.UserRole;
import com.itmo.chgk.service.LoggedUserManagementService;
import com.itmo.chgk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final LoggedUserManagementService loggedUserManagementService;

    @GetMapping("/all")
    @Operation(summary = "Получение информации обо всех пользователях")
    public Page<UserInfoResponse> getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer perPage,
                                                           @RequestParam(defaultValue = "login") String sort,
                                                           @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return userService.getAllUsers(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о конкретном пользователе")
    public UserInfoResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping("/new")
    @Operation(summary = "Создание пользователя")
    public UserInfoResponse createUser(@RequestBody @Valid UserInfoRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактирование информации о пользователе")
    public UserInfoResponse updateUser(@PathVariable Long id, @RequestBody @Valid UserInfoRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление пользователя")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Установление роли пользователя")
    public void setUserRole(@PathVariable Long id, @RequestParam UserRole role) {
        if (loggedUserManagementService.getUserDetail() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (!loggedUserManagementService.getUserDetail().getRole().equals(UserRole.ADMIN)) {
            throw new CustomException("Необходимы права администратора", HttpStatus.FORBIDDEN);
        }
        userService.setRole(id, role);
    }

}
