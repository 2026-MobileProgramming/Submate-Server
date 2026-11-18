
package com.submate.submate.service;

import com.submate.submate.dto.SignupRequest;
import com.submate.submate.entity.User;
import com.submate.submate.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long signup(SignupRequest request) {

        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // 3. 회원 객체 생성
        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getNickname()
        );

        // 4. DB에 회원 저장
        User savedUser = userRepository.save(user);

        // 5. 생성된 회원 ID 반환
        return savedUser.getId();
    }
}
