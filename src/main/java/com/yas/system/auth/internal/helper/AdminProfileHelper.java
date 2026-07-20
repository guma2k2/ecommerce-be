package com.yas.system.auth.internal.helper;

import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.entity.AdminProfile;
import com.yas.system.auth.internal.entity.CustomerProfile;
import com.yas.system.auth.internal.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminProfileHelper {

    public AdminProfile createAdminProfile(User user, String name) {
        AdminProfile adminProfile = new AdminProfile();
        adminProfile.setUser(user);
        adminProfile.setName(name);
        return adminProfile;
    }
}
