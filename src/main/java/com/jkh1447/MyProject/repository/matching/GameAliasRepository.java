package com.jkh1447.MyProject.repository.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.matching.aiMatching.GameAlias;
import java.util.Optional;

public interface GameAliasRepository extends JpaRepository<GameAlias, Long> {
  Optional<GameAlias> findByUserInput(String userInput);
}
