package com.itmo.chgk.config;

import com.itmo.chgk.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;
import static org.springframework.security.config.Customizer.withDefaults;



@AllArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = false)
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
                    //user_info
                    .requestMatchers(HttpMethod.PUT, "/userInfo/*/role").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/userInfo/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/userInfo/new").anonymous()
                    .requestMatchers("/userInfo/**").authenticated()

//                        .requestMatchers(HttpMethod.PUT, "/userInfo/role/**").hasRole("ADMIN")
//                    .requestMatchers(HttpMethod.PUT, "/userInfo/**").authenticated()
//                    .requestMatchers(HttpMethod.DELETE, "/userInfo/**").authenticated()
//                    .requestMatchers(HttpMethod.POST, "/userInfo/new").anonymous()

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


                    .requestMatchers("/users/login6").hasRole("ADMIN")
                    .requestMatchers("/users/login7").hasRole("USER")
                    .requestMatchers("/users/login8").authenticated()
                    .requestMatchers("/users/login9").permitAll()
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
