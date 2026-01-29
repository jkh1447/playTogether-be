package com.jkh1447.MyProject.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.auth.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // entity의 필드명이 기준. db속성 기준이 아니다
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    void deleteByUserId(Long userId);
}