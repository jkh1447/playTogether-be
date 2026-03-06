package com.jkh1447.MyProject.global.config.webSocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import com.jkh1447.MyProject.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import java.util.Map; 
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import com.jkh1447.MyProject.service.matching.UserInfoHelper;
import com.jkh1447.MyProject.global.utility.NetworkUtil;

@Component
@RequiredArgsConstructor
public class HandshakeAuthInterceptor implements HandshakeInterceptor {

  private final JwtUtil jwtUtil;
  private final UserInfoHelper userInfoHelper;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpRequest = servletRequest.getServletRequest();

      String token = JwtUtil.getTokenFromCookie(httpRequest, AuthConstants.TOKEN_NAME);

      if (token == null) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
      }
      
      try {
        jwtUtil.validateToken(token);
        String userId = jwtUtil.getSubjectFromToken(token);
        String nickname = userInfoHelper.getNickname(userId);
        String clientIp = NetworkUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // userId, nickname, clientIp를 추출해 세션에 저장한다.

        attributes.put("userId", userId);
        attributes.put("nickname", nickname);
        attributes.put("clientIp", clientIp);
        attributes.put("userAgent", userAgent);
        return true;
      }
      catch (Exception e) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
      }
    }
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    
  }
  
}
