package com.jkh1447.MyProject.service.users;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.dto.users.UserInfoResponse;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.domain.users.exception.UserNotFoundException;
import com.jkh1447.MyProject.domain.auth.AuthConstants;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UserRepository userRepository;

    public UserInfoResponse getMyInfo(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        return UserInfoResponse.from(user);
    }

    public String getNickname(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        return user.getNickname();
    }

    public void withdraw(Authentication authentication) {
        if (authentication == null) {
            throw new UserNotFoundException();
        }

        String userId = authentication.getName();
        if(userId.startsWith(AuthConstants.GUEST_TOKEN_PREFIX)) {
            throw new IllegalArgumentException("게스트는 탈퇴할 수 없습니다.");    
        }

        userRepository.deleteById(Long.parseLong(userId));
    }

}