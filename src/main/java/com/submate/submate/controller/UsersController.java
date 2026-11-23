package com.submate.submate.controller;

import com.submate.submate.common.ApiErrorCode;
import com.submate.submate.common.ApiException;
import com.submate.submate.dto.UserResponse;
import com.submate.submate.dto.NicknameUpdateRequest;
import com.submate.submate.entity.User;
import com.submate.submate.repository.SubscriptionRepository;
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
    private final SubscriptionRepository subscriptionRepository;

    public UsersController(UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping("/me")
    public UserResponse getMyInfo(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return toResponse(user);
    }
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateNickname(
            Authentication authentication,
            @RequestBody NicknameUpdateRequest request
    ) {
        User user = (User) authentication.getPrincipal();

        String nickname = request.getNickname();

        // 닉네임 검증
        if (nickname == null ||
                !nickname.matches("^[가-힣a-zA-Z0-9]{1,8}$")) {

            throw new ApiException(ApiErrorCode.INVALID_NICKNAME);
        }

        user.setNickname(nickname);
        userRepository.save(user);

        return ResponseEntity.ok(toResponse(user));
    }
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        userRepository.delete(user);

        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        Long userId = user.getId();
        return new UserResponse(
                userId,
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt(),
                subscriptionRepository.countByUserIdAndStatusNot(userId, "CANCELED"),
                subscriptionRepository.countByUserIdAndPlanTypeAndStatusNot(userId, "TRIAL", "CANCELED"),
                subscriptionRepository.countByUserIdAndPlanTypeAndStatus(userId, "PAID", "CANCEL_SCHEDULED")
        );
    }
}
