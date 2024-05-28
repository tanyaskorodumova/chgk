package com.itmo.chgk.security;

import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
       FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

// 1. Проверяем, нужно ли использовать этот фильтр (есть ли заголовок Bearer).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);     // иначе шаг 8, пропускаем фильтр,
            return;                  				     // и запускаем остальную цепочку
        }

        String jwt = authHeader.substring("Bearer ".length());

// 2. Проверяем, что это именно access токен, а не refresh
        String scope = jwtService.extractScope(jwt);
        if (!scope.equals("access")) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUserName(jwt);

// 3. Проверяем, можно ли извлечь username из токена
        if (username==null || username.isEmpty()){
            filterChain.doFilter(request, response);
            return;
        }

// 4. Проверяем, есть ли на данный момент аутентификация
        if (SecurityContextHolder.getContext().getAuthentication() != null){
            filterChain.doFilter(request, response);
            return;
        }

// 5. Проверяем зарегистрирован ли пользователь с таким username
        if (!userRepo.existsById(username)) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepo.findById(username).orElseThrow();
        user.getAuthorities();

// 6. Проверяем, валидность токена
        if (!jwtService.isTokenValid(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

// 7. Если шесть проверок прошли, то аутентифицируемся
        // 6.1. Получаем объект UsernamePasswordAuthenticationToken.
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        // 6.2. Устанавливаем в UsernamePasswordAuthenticationToken детали из запроса (типа IP, URI, заголовки,
        //параметры запроса и т.д.)
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 6.3. Получаем объект SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 6.4. Устанавливаем UsernamePasswordAuthenticationToken в Контекст
        context.setAuthentication(authToken);

        // 6.5. Устанавливаем Контекст в КонтекстХолдер
        SecurityContextHolder.setContext(context);

// 8. Отдаем контроль дальше по Цепочке запросов.
        filterChain.doFilter(request, response);
    }
}


