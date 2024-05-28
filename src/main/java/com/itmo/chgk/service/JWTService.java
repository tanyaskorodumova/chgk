package com.itmo.chgk.service;

import io.jsonwebtoken.*;
import java.util.Date;

public interface JWTService {
    String generateToken(String username);
    String generateRToken(String username);
    Claims extractAllClaims(String token);
    String extractUserName(String token);
    Date extractExpiration(String token);
    String extractScope(String token);
    boolean isTokenExpired(String token);
    boolean isTokenValid(String token);
    boolean isTokenExpiredButValid(String token);
}
