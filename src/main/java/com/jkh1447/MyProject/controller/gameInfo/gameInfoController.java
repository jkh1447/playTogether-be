package com.jkh1447.MyProject.controller.gameInfo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.jkh1447.MyProject.global.response.ApiResponse;
import com.jkh1447.MyProject.service.gameInfo.GameInfoService;

@Controller
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/gameInfo")
public class gameInfoController {

  private final GameInfoService gameInfoService;

  @GetMapping("")
  public ResponseEntity<?> getAllGameInfoWithoutConditions() {
    return ResponseEntity
        .ok(ApiResponse.success(gameInfoService.getAllGameInfoWithoutConditions()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getGameInfoById(@PathVariable String id) {
    return ResponseEntity.ok(ApiResponse.success(gameInfoService.getGameInfoById(id)));
  }
}
