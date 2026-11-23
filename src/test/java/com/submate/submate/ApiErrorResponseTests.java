package com.submate.submate;

import com.submate.submate.common.ApiErrorCode;
import com.submate.submate.common.ApiException;
import com.submate.submate.config.SecurityConfig;
import com.submate.submate.controller.AuthController;
import com.submate.submate.controller.EmailVerificationController;
import com.submate.submate.controller.SubscriptionController;
import com.submate.submate.entity.User;
import com.submate.submate.exception.SubscriptionNotFoundException;
import com.submate.submate.repository.UserRepository;
import com.submate.submate.service.AuthService;
import com.submate.submate.service.EmailVerificationService;
import com.submate.submate.service.JwtService;
import com.submate.submate.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, EmailVerificationController.class, SubscriptionController.class})
@Import(SecurityConfig.class)
class ApiErrorResponseTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void protectedApiRequiresValidJwt() throws Exception {
        when(jwtService.validateToken("invalid-token")).thenReturn(false);

        mvc.perform(get("/api/subscriptions"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value(ApiErrorCode.UNAUTHORIZED.message()))
                .andExpect(jsonPath("$.remainingAttempts").doesNotHaveJsonPath());

        mvc.perform(get("/api/subscriptions").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void loginFailureHasItsOwnErrorCode() throws Exception {
        when(authService.login(any())).thenThrow(new ApiException(ApiErrorCode.INVALID_CREDENTIALS));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.error").doesNotHaveJsonPath());
    }

    @Test
    void malformedJsonUsesCommonBadRequestBody() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void signupRetainsItsCreatedResponse() throws Exception {
        when(authService.signup(any())).thenReturn(42L);

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(42));
    }

    @Test
    void passwordResetUsesSpecificValidationCode() throws Exception {
        org.mockito.Mockito.doThrow(new ApiException(ApiErrorCode.INVALID_PASSWORD))
                .when(authService).resetPassword(any());

        mvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void verificationMismatchReturnsRemainingAttemptsAsNumber() throws Exception {
        when(emailVerificationService.verifyCode(any(), any(), any()))
                .thenThrow(ApiException.codeMismatch(2));

        mvc.perform(post("/api/auth/email/verify").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_MISMATCH"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.remainingAttempts").isNumber())
                .andExpect(jsonPath("$.remainingAttempts").value(2));
    }

    @Test
    void verificationAttemptLimitReturnsTooManyRequests() throws Exception {
        when(emailVerificationService.verifyCode(any(), any(), any()))
                .thenThrow(new ApiException(ApiErrorCode.TOO_MANY_ATTEMPTS));

        mvc.perform(post("/api/auth/email/verify").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_ATTEMPTS"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void missingSubscriptionReturnsNotFound() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        var session = new UsernamePasswordAuthenticationToken(user, null, List.of());
        org.mockito.Mockito.doThrow(new SubscriptionNotFoundException())
                .when(subscriptionService).deleteSubscription(1L, 99L);

        mvc.perform(delete("/api/subscriptions/99").with(authentication(session)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void unexpectedErrorReturnsGenericMessage() throws Exception {
        when(authService.signup(any())).thenThrow(new IllegalStateException("internal detail"));

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(ApiErrorCode.INTERNAL_ERROR.message()));
    }
}
