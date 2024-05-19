package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.SignInRequest;
import com.itmo.chgk.model.dto.response.JwtAuthenticationResponse;
import com.itmo.chgk.service.impl.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class AuthenticationController {
    AuthenticationService authService;

    @PostMapping("users/login")
    public JwtAuthenticationResponse login(@RequestBody SignInRequest request){
        return authService.signIn(request);
    }

    @PostMapping("users/refresh")
    public JwtAuthenticationResponse refresh(HttpServletRequest request){
        return authService.refreshToken(request);
    }
}
