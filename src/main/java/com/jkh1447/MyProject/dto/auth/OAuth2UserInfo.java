package com.jkh1447.MyProject.dto.auth;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OAuth2UserInfo {
    private String provider;
    private String providerId;
    private String email;
    private String nickname;

    public static OAuth2UserInfo of(String provider, Map<String, Object> attributes) {
        
        OAuth2UserInfo oAuth2UserInfo = null;
        if(provider.equals("google")) {
            oAuth2UserInfo = OAuth2UserInfo.builder()
                    .provider(provider)
                    .providerId((String) attributes.get("sub"))
                    .email((String) attributes.get("email"))
                    .nickname((String) attributes.get("name"))
                    .build();
        }
        else if(provider.equals("naver")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            oAuth2UserInfo = OAuth2UserInfo.builder()
                    .provider(provider)
                    .providerId((String) response.get("id"))
                    .email((String) response.get("email"))
                    .nickname((String) response.get("nickname"))
                    .build();
        }
        return oAuth2UserInfo;
    }
}