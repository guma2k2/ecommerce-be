package com.yas.system.auth.internal.validator;

import com.yas.system.auth.internal.dto.request.UserRequest;
import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.repository.RoleRepository;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserValidator {

    UserRepository userRepository;
    RoleRepository roleRepository;

    public List<Role> validateAndResolveRoles(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new ApplicationException(ErrorCode.ROLE_NOT_FOUND);
        }
        return roles;
    }

    public void validateCreate(UserRequest request) {
        if (userRepository.findByEmail(request.email().trim()).isPresent()) {
            throw new ApplicationException(ErrorCode.INVALID_EMAIL);
        }
    }

    public void validateEmailUpdate(User user, String newEmail) {
        if (!user.getEmail().equalsIgnoreCase(newEmail.trim())) {
            if (userRepository.findByEmail(newEmail.trim()).isPresent()) {
                throw new ApplicationException(ErrorCode.INVALID_EMAIL);
            }
        }
    }
}

