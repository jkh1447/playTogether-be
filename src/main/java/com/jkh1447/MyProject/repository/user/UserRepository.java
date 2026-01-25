package com.jkh1447.MyProject.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jkh1447.MyProject.domain.users.Users;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByLoginId(String loginId);
}
