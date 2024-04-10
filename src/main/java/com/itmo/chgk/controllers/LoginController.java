package com.itmo.chgk.controllers;

import com.itmo.chgk.service.LoggedUserManagementService;
import com.itmo.chgk.service.impl.LoginProcessorImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class LoginController {
    private final LoginProcessorImpl loginProcessor;
    private final LoggedUserManagementService loggedUserManagementService;

    @GetMapping("/login")
    @Operation(summary = "Проверка авторизации")
    public String loginGet() {
        if (loggedUserManagementService.getUser() != null) {
            return "Вход произведен ранее под логином " + loggedUserManagementService.getUser().getLogin();
        }
        else {
            return "Введите имя пользователя и пароль";
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public String loginPost(@RequestParam String login, @RequestParam String password) {
        if (loggedUserManagementService.getUser() != null) {
            return "Вход произведен ранее под логином " + loggedUserManagementService.getUser().getLogin();
        }
        else {
            loginProcessor.setLogin(login);
            loginProcessor.setPassword(password);

            if (loginProcessor.login()) {
                return "Добро пожаловать, " + login + "!";
            } else {
                return "Логин или пароль введены неверно";
            }
        }
    }

    @GetMapping("/logout")
    @Operation(summary = "Выход из системы")
    public String logout() {
        loggedUserManagementService.setUser(null);
        loggedUserManagementService.setTeamId(null);
        loggedUserManagementService.setTournamentId(null);
        return "Выход произведен";
    }
}
