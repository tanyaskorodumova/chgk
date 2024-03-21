package com.itmo.chgk.model.db.repository;


import com.itmo.chgk.model.db.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepo  extends JpaRepository<Question, Long> {
}
