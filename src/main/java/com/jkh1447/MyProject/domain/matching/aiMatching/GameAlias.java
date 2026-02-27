package com.jkh1447.MyProject.domain.matching.aiMatching;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "game_alias", indexes = @Index(name = "idx_user_input", columnList = "userInput"))
@Getter
@NoArgsConstructor
public class GameAlias {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String userInput;

  @Column(nullable = false)
  private String standardName;

  public GameAlias(String userInput, String standardName) {
    this.userInput = userInput;
    this.standardName = standardName;
  }
}
