package com.jkh1447.MyProject.service.users;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.dto.users.UserResponse;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.domain.users.exception.UserException;
import com.jkh1447.MyProject.domain.users.exception.UserErrorCode;
import com.jkh1447.MyProject.domain.users.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UserRepository userRepository;

    public UserResponse getMyInfo(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        return UserResponse.from(user);
    }

    public String getNickname(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        return user.getNickname();
    }

}