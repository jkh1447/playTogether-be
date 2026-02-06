package com.jkh1447.MyProject.dto.users;

import com.jkh1447.MyProject.domain.users.Users;
import lombok.Builder;
import com.jkh1447.MyProject.domain.auth.Role;

@Builder
public record UserResponse(
    String nickname,
    Role role,
    String userId
) {
    public static UserResponse from(Users user) {
        return new UserResponse(user.getNickname(), user.getRole(), user.getId().toString());
    }
}
