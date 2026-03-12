package com.jkh1447.MyProject.dto.chating;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter          // 필드 값을 읽기 위해 필요
@Builder         // 객체 생성을 위해 필요
@AllArgsConstructor // 빌더 패턴 사용 시 필수 (모든 필드를 인자로 받는 생성자)
public class ChatMessageLogDto {
    private String id;
    private String roomId;
    private String senderId;
    private String senderNickname;
    private String content;
    private String clientIp;
    private String userAgent;
    private boolean isPreserved;
    private String createdAt;
}