package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.GameParticipant;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.enums.ParticipantStatus;
import com.itmo.chgk.model.enums.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameParticipantRepo  extends JpaRepository<GameParticipant, Long> {

    @Query(nativeQuery = true, value = "select * from game_participants p left join tournaments_games t on p.game_id = t.games_id " +
            "where t.tournament_id = :tournament and p.status <> 2")
    Page<GameParticipant> findAllByTournament(@Param("tournament") Tournament tournament, Pageable pageable);

    Page<GameParticipant> findAllByGameAndStatusIsNot(Game game, ParticipantStatus status, Pageable pageable);

    GameParticipant findByGameAndParticipant(Game game, Team participant);

    Page<GameParticipant> findAllByGameAndStatus(Game game, ParticipantStatus status, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from game_participants " +
            "left join tournaments_games on game_participants.game_id = tournaments_games.games_id " +
            "left join games g on game_participants.game_id = g.id " +
            "where participant_id = :participant and game_participants.status <> :status " +
            "and tournaments_games.tournament_id = :tournament and stage = :stage")
    GameParticipant findByParticipantAndStatusIsNotAndTournament(@Param("participant") Team participant,
                                                            @Param("status") ParticipantStatus status,
                                                            @Param("tournament") Tournament tournament,
                                                            @Param("stage") Stage stage);
}
