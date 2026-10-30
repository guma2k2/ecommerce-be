package com.yas.system.auth;

import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.enumeration.OauthProvider;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.validator.AuthValidator;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthValidatorTest {

    @Mock
    UserRepository userRepository;

    AuthValidator authValidator;

    @BeforeEach
    void setUp() {
        authValidator = new AuthValidator(userRepository);
    }

    @Test
    @DisplayName("validateSignUp throws INVALID_EMAIL when email already exists")
    void validateSignUp_emailExists() {
        SignUpRequest request = new SignUpRequest("existing@yas.com", "Password@123", "Test User", "EN");
        when(userRepository.findByEmail("existing@yas.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authValidator.validateSignUp(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_EMAIL);
    }

    @Test
    @DisplayName("validateSignUp succeeds when email is available")
    void validateSignUp_success() {
        SignUpRequest request = new SignUpRequest("new@yas.com", "Password@123", "Test User", "EN");
        when(userRepository.findByEmail("new@yas.com")).thenReturn(Optional.empty());

        assertThatCode(() -> authValidator.validateSignUp(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateOAuthState throws UNCATEGORIZED when state does not match")
    void validateOAuthState_stateMismatch() {
        assertThatThrownBy(() -> authValidator.validateOAuthState("code123", "state1", "state2"))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("validateOAuthState succeeds when state matches")
    void validateOAuthState_success() {
        assertThatCode(() -> authValidator.validateOAuthState("code123", "state1", "state1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateProvider throws INVALID_PROVIDER when provider mismatches")
    void validateProvider_mismatch() {
        User user = new User();
        user.setProvider(OauthProvider.GOOGLE);

        assertThatThrownBy(() -> authValidator.validateProvider(user, OauthProvider.GITHUB))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROVIDER);
    }

    @Test
    @DisplayName("validateMfaCode throws INVALID_CODE when false")
    void validateMfaCode_invalid() {
        assertThatThrownBy(() -> authValidator.validateMfaCode(false))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CODE);
    }
}
