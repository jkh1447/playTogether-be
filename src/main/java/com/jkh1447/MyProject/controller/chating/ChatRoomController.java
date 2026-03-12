package com.jkh1447.MyProject.controller.chating;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jkh1447.MyProject.dto.chating.ParticipantsDto;
import com.jkh1447.MyProject.global.response.ApiResponse;
import com.jkh1447.MyProject.service.chating.ChatRoomService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/room/{roomId}/participants")
    public ResponseEntity<ApiResponse<ParticipantsDto>> getParticipants(
            @PathVariable String roomId) {

        ParticipantsDto participants = chatRoomService.getParticipants(roomId);

        return ResponseEntity.ok(ApiResponse.success(participants));
    }
}
