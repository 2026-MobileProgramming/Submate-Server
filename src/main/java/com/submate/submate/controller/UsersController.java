package com.submate.submate.controller;

import com.submate.submate.dto.UserResponse;
import com.submate.submate.dto.NicknameUpdateRequest;
import com.submate.submate.entity.User;
import com.submate.submate.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserRepository userRepository;

    public UsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserResponse getMyInfo(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
    @PatchMapping("/me")
    public ResponseEntity<?> updateNickname(
            Authentication authentication,
            @RequestBody NicknameUpdateRequest request
    ) {
        User user = (User) authentication.getPrincipal();

        String nickname = request.getNickname();

        // 닉네임 검증
        if (nickname == null ||
                !nickname.matches("^[가-힣a-zA-Z0-9]{1,8}$")) {

            return ResponseEntity.badRequest().body(
                    java.util.Map.of("code", "INVALID_NICKNAME")
            );
        }

        user.setNickname(nickname);
        userRepository.save(user);

        return ResponseEntity.ok(
                new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getCreatedAt()
                )
        );
    }
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        userRepository.delete(user);

        return ResponseEntity.noContent().build();
    }
}