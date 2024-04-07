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

    @Query(nativeQuery = true, value = "select * from question_results qr where qr.question_id = :questionId and qr.team_id = :teamId")
    QuestionResult findByQuestionAndTeam(@Param("questionId") Long questionId, @Param("teamId") Long teamId);

    @Query("select qr from QuestionResult qr where qr.question.game.id = :gameId")
    Page<QuestionResult> findAllByGameId(@Param("gameId") Long gameId, Pageable pageable);

    @Query("select qr from QuestionResult qr where qr.question.game.id = :gameId")
    List<QuestionResult> findAllByGameId(@Param("gameId") Long gameId);
}
