package com.itmo.chgk.service;

import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.dto.request.UserRequest;
import com.itmo.chgk.model.dto.response.JwtAuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    JwtAuthenticationResponse signIn(UserRequest request) throws CustomException;
    JwtAuthenticationResponse refreshToken(HttpServletRequest request) throws CustomException;
}
