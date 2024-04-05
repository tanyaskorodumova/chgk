package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.GameQuestion;
import com.itmo.chgk.model.db.entity.QuestionResult;
import com.itmo.chgk.model.db.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionResultRepo extends JpaRepository<QuestionResult, Long> {
    Page<QuestionResult> findAllByQuestion(GameQuestion question, Pageable pageable);

    QuestionResult findByQuestionAndTeam(GameQuestion question, Team team);

    @Query(nativeQuery = true, value = "select * from question_results left join game_questions gq on gq.id = question_results.question_id " +
            "where gq.game_id = :gameId")
    Page<QuestionResult> findAllByGameId(@Param("gameId") Long gameId, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from question_results left join game_questions gq on gq.id = question_results.question_id " +
            "where gq.game_id = :gameId")
    List<QuestionResult> findAllByGameId(@Param("gameId") Long gameId);
}
