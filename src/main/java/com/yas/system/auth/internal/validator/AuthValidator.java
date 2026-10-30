package com.yas.system.auth.internal.validator;

import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.enumeration.OauthProvider;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthValidator {

    UserRepository userRepository;

    public void validateSignUp(SignUpRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            throw new ApplicationException(ErrorCode.INVALID_EMAIL);
        }
        if (userRepository.findByEmail(request.email().trim()).isPresent()) {
            throw new ApplicationException(ErrorCode.INVALID_EMAIL);
        }
    }

    public void validateOAuthState(String code, String state, String savedState) {
        if (code == null || state == null || !state.equals(savedState)) {
            throw new ApplicationException(ErrorCode.UNCATEGORIZED);
        }
    }

    public void validateProvider(User user, OauthProvider provider) {
        if (!Objects.equals(user.getProvider(), provider)) {
            throw new ApplicationException(ErrorCode.INVALID_PROVIDER);
        }
    }

    public void validateMfaCode(boolean isCodeValid) {
        if (!isCodeValid) {
            throw new ApplicationException(ErrorCode.INVALID_CODE);
        }
    }
}

