package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.enums.GameStatus;
import com.itmo.chgk.model.enums.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepo  extends JpaRepository<Game, Long> {
    @Query("select g from Game g where g.tournament = :tournament and g.stage = :stage and g.status <> :status")
    List<Game> findAllByTournamentAndStageAndStatusIsNot(@Param("tournament") Tournament tournament,
                                                         @Param("stage") Stage stage,
                                                         @Param("status") GameStatus status);

    @Query("select g from Game g where g.tournament = :tournament and g.status <> :status")
    Page<Game> findAllByTournamentAndStatusIsNot(@Param("tournament") Tournament tournament,
                                                 @Param("status") GameStatus status,
                                                 Pageable pageable);


}
