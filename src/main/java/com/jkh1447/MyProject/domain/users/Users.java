package com.jkh1447.MyProject.domain.users;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import com.jkh1447.MyProject.domain.auth.Role;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String provider; // "google", "naver" 등

    @Column(nullable = false)
    private String providerId; // 구글의 'sub'이나 네이버의 'id' 값

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ROLE_USER, ROLE_GUEST 등

    // 나중에 네이버 연동 시 닉네임이나 프로필 이미지가 바뀔 수 있으므로 업데이트 메서드 추가
    public void updateInfo(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
