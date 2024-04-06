package com.itmo.chgk.model.db.repository;


import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepo  extends JpaRepository<Question, Long> {
    Page<Question> findAllByStatus(Pageable pageable, QuestionStatus status);

    @Query(nativeQuery = true, value = "select * from questions inner join game_questions on questions.id = game_questions.question_id " +
            "left join games on game_questions.game_id = games.id " +
            "where complexity >= :minComplexity and complexity <= :maxComplexity and questions.status = 2 " +
            "and games.status in (2, 3) order by random() limit :number")
    List<Question> findByQuestionPackRequest(@Param("minComplexity") Integer minComplexity,
                                             @Param("maxComplexity") Integer maxComplexity,
                                             @Param("number") Integer number);

    @Query(value = "select * from questions where (status = 0 and created_At > (current_date-31)) or (status = 1 and updated_At > (current_date-31))",
            nativeQuery = true)
    Page<Question> findNew(Pageable pageable);

    @Query(nativeQuery = true, value = "with old_games as (select distinct game_id from game_participants gp inner join games g on gp.game_id = g.id and g.status <> 4 " +
            "where gp.participant_id in (select distinct gpun.participant_id from game_participants gpun where gpun.game_id = :gameId and gpun.status <> 2)) " +
            ", old_questions as (select distinct question_id from game_questions where game_id in (select * from old_games)) " +
            "select * from questions where id not in (select * from old_questions) and complexity >= :minComplexity and complexity <= :maxComplexity " +
            "and status = 2 and user_id not in (select distinct gpun.participant_id from game_participants gpun where gpun.game_id = :gameId) " +
            "order by random(), complexity limit :number")
    Page<Question> findGamePack(Pageable pageable, @Param("gameId") Long gameId,
                                @Param("minComplexity") Integer minComplexity,
                                @Param("maxComplexity") Integer maxComplexity,
                                @Param("number") Integer number);

    @Query(nativeQuery = true, value = "select * from questions where questions.id not in (select distinct game_questions.question_id from game_questions) " +
            "and complexity >= :minComplexity and complexity <= :maxComplexity order by random(), complexity limit :number")
    Page<Question> findFedGamePack(Pageable pageable,
                                   @Param("minComplexity") Integer minComplexity,
                                   @Param("maxComplexity") Integer maxComplexity,
                                   @Param("number") Integer number);
}
