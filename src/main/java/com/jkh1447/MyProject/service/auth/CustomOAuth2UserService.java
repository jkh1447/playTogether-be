package com.jkh1447.MyProject.service.auth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.auth.OAuth2UserInfo;
import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.domain.auth.Role;
import com.jkh1447.MyProject.domain.users.Users;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfo.of(provider, oAuth2User.getAttributes());

        saveOrUpdate(userInfo);

        return oAuth2User;
    }

    private Users saveOrUpdate(OAuth2UserInfo userInfo) {
        Users user = userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .map(entity -> {
                    entity.updateLastLoginAt();
                    entity.updateInfo(userInfo.getEmail(), userInfo.getNickname());
                    return entity;
                })
                .orElseGet(() -> {
                    return Users.builder()
                            .provider(userInfo.getProvider())
                            .providerId(userInfo.getProviderId())
                            .email(userInfo.getEmail())
                            .nickname(userInfo.getNickname())
                            .role(Role.USER)
                            .build();
                });
        return userRepository.save(user);
    }
}
