package com.jkh1447.MyProject.dto.users;

import com.jkh1447.MyProject.domain.users.Users;
import lombok.Builder;
import com.jkh1447.MyProject.domain.auth.Role;

@Builder
public record UserInfoResponse(
    String nickname,
    Role role,
    String userId,
    String email
) {
    public static UserInfoResponse from(Users user) {
        return new UserInfoResponse(user.getNickname(), user.getRole(), user.getId().toString(), user.getEmail());
    }
}
