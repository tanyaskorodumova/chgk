package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.Result;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.enums.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResultRepo  extends JpaRepository<Result, Long> {

    Page<Result> findAllByGame(Game game, Pageable pageable);

    Result findByGameAndTeam(Game game, Team team);

    @Query(value = "select R from Result R where R.game.tournament = :tournament and R.game.stage = :stage order by R.place ASC, R.points DESC")
    List<Result> findAllByTournamentAndStage(Tournament tournament, Stage stage);
}
