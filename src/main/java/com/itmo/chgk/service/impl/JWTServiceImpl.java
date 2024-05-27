package com.itmo.chgk.service.impl;

import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.service.JWTService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;


@RequiredArgsConstructor
@Component
public class JWTServiceImpl implements JWTService {
    private final UserRepo repository;
    @Value("${jwt.signing_key}")
    public String jwtSigningKey;

// 1. Методы генерации токена
    // метод генерирует токен
    public String generateToken(String username) {

        return Jwts.builder()
                .setSubject(username)
                .claim("scope", "access")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*15))
                .signWith(SignatureAlgorithm.HS256, jwtSigningKey)
                .compact();
    }

    // метод генерирует рефреш-токен
    public String generateRToken(String username) {

        String RToken = Jwts.builder()
                .setSubject(username)
                .claim("scope", "refresh")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*24*7))
                .signWith(SignatureAlgorithm.HS256, jwtSigningKey)
                .compact();

        Optional<User> OpEmp= repository.findById(username);
        if(OpEmp.isEmpty())
            throw new CustomException("Субъект токена отсутствует в БД", HttpStatus.NOT_FOUND);

        User user = OpEmp.get();
        user.setToken(RToken);
        repository.save(user);

        return RToken;
    }

// 2. Методы извлечения сведений из токена

    // Метод извлекает из токена все Претензии (пары ключ-значение их Полезной нагрузки)
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtSigningKey).build()
                    .parseClaimsJws(token)
                    .getBody();

        }
        catch (ClaimJwtException e){
            return e.getClaims();
        }

        catch (JwtException e){
            throw new CustomException("invalid token", HttpStatus.BAD_REQUEST);
        }
    }

    // Метод извлекает одну конкретную Претензию (дату истечения или имя пользователя)
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    // Метод извлекает из токена имя пользователя
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Метод извлекает из токена время истечения
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // метод извлекает "scope" (тип) токена
    public String extractScope(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("scope", String.class);
    }

// 3. Методы валидации токенов

    // метод проверяет, не истек ли срок действия токена
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // метод проверяет валидность токена
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSigningKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // метод проверяет, что у токена истек срок, но в остальном он валиден
    public boolean isTokenExpiredButValid(String token) {
        try {
            // Парсинг JWT токена и получение объекта Claims
            Jwts.parser()
                .setSigningKey(jwtSigningKey).build()
                .parseClaimsJws(token)
                .getBody();

        } catch (SignatureException e) {
            return false;   // подпись токена подделана


        } catch (ExpiredJwtException e) {
            return true;   // подпись токена не подделана, срок истек


        } catch (Exception e) {
            return false;   // токен недействителен по другим причинам
        }

        return false;      // токен действителен (не истек и не подделан)
    }
}
