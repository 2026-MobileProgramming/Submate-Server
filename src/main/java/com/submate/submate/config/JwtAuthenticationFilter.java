package com.submate.submate.config;

import com.submate.submate.entity.User;
import com.submate.submate.repository.UserRepository;
import com.submate.submate.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // Authorization 헤더에 토큰이 있는지 확인
        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring(7);

            // 토큰이 유효한지 확인
            if (jwtService.validateToken(token)) {

                Long userId = jwtService.extractUserId(token);

                // 토큰에 담긴 userId로 사용자 조회
                User user = userRepository.findById(userId)
                        .orElse(null);

                if (user != null) {

                    // 인증된 사용자 정보 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.emptyList()
                            );

                    // Spring Security에 인증 정보 등록
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        }

        // 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}