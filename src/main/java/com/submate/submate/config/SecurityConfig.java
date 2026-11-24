package com.submate.submate.config;

import com.submate.submate.common.ApiErrorCode;
import com.submate.submate.dto.ErrorResponse;
import com.submate.submate.repository.UserRepository;
import com.submate.submate.service.JwtService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtService,
                userRepository
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JsonMapper jsonMapper)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/health",
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/email/send",
                                "/api/auth/email/verify",
                                "/api/auth/password/reset",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authenticationException) -> {
                                    ApiErrorCode error = ApiErrorCode.UNAUTHORIZED;
                                    response.setStatus(error.status().value());
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    jsonMapper.writeValue(
                                            response.getOutputStream(),
                                            new ErrorResponse(error.name(), error.message())
                                    );
                                }
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
