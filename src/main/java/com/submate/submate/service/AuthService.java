
package com.submate.submate.service;

import com.submate.submate.dto.SignupRequest;
import com.submate.submate.dto.LoginRequest;
import com.submate.submate.dto.LoginResponse;
import com.submate.submate.service.JwtService;
import com.submate.submate.entity.EmailVerification;
import com.submate.submate.repository.EmailVerificationRepository;
import com.submate.submate.entity.User;
import com.submate.submate.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Long signup(SignupRequest request) {

        // 1. 이메일 정리
        String email = request.getEmail().trim().toLowerCase();

        // 2. 이메일 인증 확인
        EmailVerification verification = emailVerificationRepository
                .findFirstByEmailAndPurposeAndStatusOrderByCreatedAtDesc(
                        email,
                        "SIGNUP",
                        "VERIFIED"
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("EMAIL_NOT_VERIFIED")
                );

        // 3. 중복 이메일 확인
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("DUPLICATE_EMAIL");
        }

        // 4. 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("PASSWORD_MISMATCH");
        }

        // 5. 비밀번호 형식 확인
        String password = request.getPassword();

        if (password == null ||
                !Pattern.matches(
                        "^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~]{8,20}$",
                        password
                )) {
            throw new IllegalArgumentException("INVALID_PASSWORD");
        }

        // 6. 닉네임 형식 확인
        String nickname = request.getNickname();

        if (nickname == null ||
                !Pattern.matches("^[가-힣a-zA-Z0-9]{1,8}$", nickname)) {
            throw new IllegalArgumentException("INVALID_NICKNAME");
        }

        // 7. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 8. 회원 생성
        User user = new User(
                email,
                encodedPassword,
                nickname
        );

        User savedUser = userRepository.save(user);

        // 9. 이메일 인증 완료 상태를 사용 완료로 변경
        verification.setStatus("USED");

        // 10. 회원 ID 반환
        return savedUser.getId();
    }

    public LoginResponse login(LoginRequest request) {

        // 1. 이메일로 사용자 찾기
        User user = userRepository.findByEmail(
                request.getEmail().trim().toLowerCase()
        ).orElseThrow(() ->
                new IllegalArgumentException("INVALID_CREDENTIALS")
        );

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }

        // 3. JWT 생성
        String accessToken = jwtService.generateToken(user.getId());

        // 4. 응답에 넣을 사용자 정보 생성
        LoginResponse.UserInfo userInfo =
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname()
                );

        // 5. 로그인 응답 반환
        return new LoginResponse(
                accessToken,
                "Bearer",
                604800,
                userInfo
        );
    }

}
