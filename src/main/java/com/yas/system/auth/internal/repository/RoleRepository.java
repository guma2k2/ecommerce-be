package com.yas.system.auth.internal.repository;

import com.yas.system.auth.internal.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @EntityGraph(value = "Role.permissions")
    Optional<Role> findByName(String name);

    @Override
    @EntityGraph(value = "Role.permissions")
    Optional<Role> findById(Integer id);

    boolean existsByName(String name);

    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE role_id = :roleId", nativeQuery = true)
    void deleteUserRolesByRoleId(@Param("roleId") Integer roleId);
}
