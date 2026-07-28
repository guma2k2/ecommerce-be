package com.yas.system.common.config;

import com.yas.system.auth.internal.entity.AdminProfile;
import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.enumeration.OauthProvider;
import com.yas.system.auth.internal.helper.AdminProfileHelper;
import com.yas.system.auth.internal.repository.AdminProfileRepository;
import com.yas.system.auth.internal.repository.RoleRepository;
import com.yas.system.auth.internal.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class InitialConfig implements CommandLineRunner {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    AdminProfileRepository adminProfileRepository;
    AdminProfileHelper adminProfileHelper;

    @Override
    public void run(String... args) {
        Role superAdminRole = roleRepository.findByName("ROLE_SUPERADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_SUPERADMIN");
                    role.setAllowGetAll(true);
                    return roleRepository.save(role);
                });

        if (userRepository.findByEmail("superadminyas@yopmail.com").isEmpty()) {
            User superAdmin = new User();
            superAdmin.setEmail("superadminyas@yopmail.com");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setVerified(true);
            superAdmin.setProvider(OauthProvider.LOCAL);
            superAdmin.setRoles(Set.of(superAdminRole));
            userRepository.save(superAdmin);

            // Save admin profile
            AdminProfile adminProfile = adminProfileHelper.createAdminProfile(superAdmin, "Super Admin");
            adminProfileRepository.save(adminProfile);
        }
    }
}