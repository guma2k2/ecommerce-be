package com.yas.system.auth;

import com.yas.system.auth.internal.dto.request.UserRequest;
import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.repository.RoleRepository;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.validator.UserValidator;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    UserValidator userValidator;

    @BeforeEach
    void setUp() {
        userValidator = new UserValidator(userRepository, roleRepository);
    }

    @Test
    @DisplayName("validateAndResolveRoles throws ROLE_NOT_FOUND if some roles are missing")
    void validateAndResolveRoles_missingRole() {
        List<Integer> roleIds = List.of(1, 2);
        when(roleRepository.findAllById(roleIds)).thenReturn(List.of(new Role()));

        assertThatThrownBy(() -> userValidator.validateAndResolveRoles(roleIds))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    @DisplayName("validateAndResolveRoles returns roles when all exist")
    void validateAndResolveRoles_success() {
        List<Integer> roleIds = List.of(1, 2);
        List<Role> roles = List.of(new Role(), new Role());
        when(roleRepository.findAllById(roleIds)).thenReturn(roles);

        List<Role> result = userValidator.validateAndResolveRoles(roleIds);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("validateCreate throws INVALID_EMAIL when email is taken")
    void validateCreate_emailTaken() {
        UserRequest request = new UserRequest("taken@yas.com", "Password@123", "Test User", List.of(1));
        when(userRepository.findByEmail("taken@yas.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userValidator.validateCreate(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_EMAIL);
    }

    @Test
    @DisplayName("validateEmailUpdate throws INVALID_EMAIL when changing to existing email")
    void validateEmailUpdate_conflict() {
        User user = new User();
        user.setEmail("current@yas.com");

        when(userRepository.findByEmail("another@yas.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userValidator.validateEmailUpdate(user, "another@yas.com"))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_EMAIL);
    }

    @Test
    @DisplayName("validateEmailUpdate succeeds when email unchanged")
    void validateEmailUpdate_sameEmail() {
        User user = new User();
        user.setEmail("current@yas.com");

        assertThatCode(() -> userValidator.validateEmailUpdate(user, "current@yas.com"))
                .doesNotThrowAnyException();
    }
}
