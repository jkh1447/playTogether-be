package com.jkh1447.MyProject.domain.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Table(name = "refresh_token")
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshToken {

    @Id
    private String userId;

    @Column(nullable = false)
    private String refreshToken;

    public void updateToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}