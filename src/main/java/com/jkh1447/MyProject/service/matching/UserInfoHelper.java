package com.jkh1447.MyProject.service.matching;

import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.service.users.UsersService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserInfoHelper {
    
    private final UsersService usersService;

    public String getNickname(String userId) {
        if (userId == null){
            return "UNKNOWN";
        }

        if(userId.startsWith(AuthConstants.GUEST_TOKEN_PREFIX)) {
            return generateGuestNickName(userId);
        }

        try {
            return usersService.getNickname(Long.parseLong(userId));
        } catch (NumberFormatException e) {
            return "UNKNOWN";
        }
    }

    private String generateGuestNickName(String guestUserId) {
        String gusetIdPart = guestUserId.split("_")[1].substring(0, MatchingConstants.GUEST_NICKNAME_LENGTH);
        return AuthConstants.GUEST_NICKNAME_PREFIX + gusetIdPart;
    }

    
}
