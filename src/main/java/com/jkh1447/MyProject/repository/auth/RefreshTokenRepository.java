package com.jkh1447.MyProject.repository.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.jkh1447.MyProject.domain.auth.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // entity의 필드명이 기준. db속성 기준이 아니다
    Optional<RefreshToken> findByUserId(String userId);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    void deleteByUserId(String userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.refreshToken = :token WHERE r.userId = :userId")
    int updateRefreshToken(@Param("userId") String userId, @Param("token") String token);
}