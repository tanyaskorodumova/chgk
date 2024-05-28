package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.UserRequest;
import com.itmo.chgk.model.dto.response.JwtAuthenticationResponse;
import com.itmo.chgk.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя в систему")
    public JwtAuthenticationResponse login(@RequestBody UserRequest request){
        return authenticationService.signIn(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Получение нового access токена")
    public JwtAuthenticationResponse refresh(HttpServletRequest request){
        return authenticationService.refreshToken(request);
    }
}
