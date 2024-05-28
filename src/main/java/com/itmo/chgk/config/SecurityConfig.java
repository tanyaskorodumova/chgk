package com.itmo.chgk.config;

import com.itmo.chgk.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.sql.DataSource;
import static org.springframework.security.config.Customizer.withDefaults;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    DataSource dataSource;
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(
                authorize -> authorize
                    .requestMatchers("/auth/**").anonymous()

                    // users
                    .requestMatchers(HttpMethod.GET, "/users/all").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/users/new").anonymous()
                    .requestMatchers(HttpMethod.PUT, "/users/password").authenticated()
                    .requestMatchers(HttpMethod.GET, "/users/**").authenticated()
                    .requestMatchers("/users/**").hasRole("ADMIN")

                    //user_info
                    .requestMatchers("/userInfo/**").authenticated()

                    //tournament
                    .requestMatchers(HttpMethod.GET, "/tournaments/**").permitAll()
                    .requestMatchers("/tournaments/**").hasAnyRole("ADMIN", "ORGANIZER")

                    //teams
                    .requestMatchers(HttpMethod.GET, "/teams/**").permitAll()
                    .requestMatchers("/teams/*/deleteMember/*").authenticated()
                    .requestMatchers("/teams/new"). authenticated()
                    .requestMatchers("/teams/**").hasAnyRole("CAPTAIN", "VICECAPTAIN", "ADMIN")

                    //questions
                    .requestMatchers("/questions/approve/**").hasRole("ADMIN")
                    .requestMatchers("/questions/**").authenticated()

                    //games
                    .requestMatchers("/games/*/participants/delete/*").hasAnyRole("ORGANIZER", "VICECAPTAIN", "CAPTAIN", "ADMIN")
                    .requestMatchers("/games/*/participants/approve/*").hasAnyRole("VICECAPTAIN", "CAPTAIN", "ADMIN")
                    .requestMatchers("/games/*/participants/add/*").hasAnyRole("VICECAPTAIN", "CAPTAIN", "ADMIN")
                    .requestMatchers("/games/*/participants/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/games/*").permitAll()
                    .requestMatchers("/games/*/results/final").permitAll()
                    .requestMatchers("/games/**").hasAnyRole("ORGANIZER", "ADMIN")

                    .anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(withDefaults())
            .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
