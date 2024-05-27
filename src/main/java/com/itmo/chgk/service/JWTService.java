package com.itmo.chgk.service;

import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.User;
import io.jsonwebtoken.*;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

public interface JWTService {
    public String generateToken(String username);
    public String generateRToken(String username);
    Claims extractAllClaims(String token);
    public String extractUserName(String token);
    public Date extractExpiration(String token);
    public String extractScope(String token);
    public boolean isTokenExpired(String token);
    public boolean isTokenValid(String token);
    public boolean isTokenExpiredButValid(String token);
}
