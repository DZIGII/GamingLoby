package com.raf.gaminglobbyuserservice.repository;

import com.raf.gaminglobbyuserservice.model.Role;
import com.raf.gaminglobbyuserservice.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleByName(RoleName roleName);
}
