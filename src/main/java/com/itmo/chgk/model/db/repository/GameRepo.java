package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.enums.GameStatus;
import com.itmo.chgk.model.enums.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepo  extends JpaRepository<Game, Long> {
    List<Game> findAllByTournamentAndStageAndStatusIsNot(Tournament tournament, Stage stage, GameStatus status);
}
