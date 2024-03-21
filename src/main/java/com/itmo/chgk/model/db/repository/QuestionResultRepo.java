package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.QuestionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionResultRepo extends JpaRepository<QuestionResult, Long> {
}
