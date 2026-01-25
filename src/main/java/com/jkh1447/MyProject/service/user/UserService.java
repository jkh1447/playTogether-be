package com.jkh1447.MyProject.service.user;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;

import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.dto.user.UserSignupRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;

    public void signup(UserSignupRequest request) {
        Users user = new Users();
        user.setLoginId(request.loginId());
        user.setPassword(request.password());
        user.setEmail(request.email());
        user.setNickname(request.nickname());
        
        userRepository.save(user);
    }
}
