package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.GameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameQuestionRepo  extends JpaRepository<GameQuestion, Long> {
}
