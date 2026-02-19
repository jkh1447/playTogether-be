package com.jkh1447.MyProject.repository.gameInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.gameInfo.GameInfo;

public interface gameInfoRepository extends JpaRepository<GameInfo, String> {
  
}
