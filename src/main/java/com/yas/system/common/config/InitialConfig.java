package com.yas.system.common.config;

import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.enums.OauthProvider;
import com.yas.system.auth.internal.repository.RoleRepository;
import com.yas.system.auth.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class InitialConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("SUPER_ADMIN");
                    role.setAllowGetAll(true);
                    return roleRepository.save(role);
                });

        if (userRepository.findByEmail("superadmin@yas.com").isEmpty()) {
            User superAdmin = new User();
            superAdmin.setEmail("superadmin@yas.com");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setName("Super Admin");
            superAdmin.setVerified(true);
            superAdmin.setProvider(OauthProvider.LOCAL);
            superAdmin.setRoles(Set.of(superAdminRole));
            userRepository.save(superAdmin);
        }
    }
}