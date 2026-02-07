package com.jkh1447.MyProject.engine;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.service.matching.MatchingEngineService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    private final MatchingEngineService matchingEngineService;

    @Scheduled(fixedDelay = 2000)
    public void run(){
        matchingEngineService.processMatching();
    }

}
