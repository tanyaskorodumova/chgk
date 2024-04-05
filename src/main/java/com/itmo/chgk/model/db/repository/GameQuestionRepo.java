package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.GameQuestion;
import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.enums.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GameQuestionRepo extends JpaRepository<GameQuestion, Long> {

    Page<GameQuestion> findAllByGame(Game game, Pageable pageable);
    List<GameQuestion> findAllByGame(Game game);

    @Query(value = "select gq from GameQuestion gq inner join Game g on g = gq.game where g.status <> :status and gq.question = :question")
    GameQuestion findByQuestionAndGameStatusIsNot(@Param("question") Question question, @Param("status") GameStatus status);

    GameQuestion findByGameAndQuestion(Game game, Question question);

    List<GameQuestion> findAllByGameAndRoundAfter(Game game, Integer round);

    GameQuestion findByGameAndRound(Game game, Integer round);

}
