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

import java.util.List;

public interface GameParticipantRepo  extends JpaRepository<GameParticipant, Long> {
    @Query(nativeQuery = true,  value = "select * from game_participants p left join tournaments_games t on p.game_id = t.games_id " +
            "where t.tournament_id = :tournament and p.status <> 2")
    Page<GameParticipant> findAllByTournament(@Param("tournament") Long tournament, Pageable pageable);

    Page<GameParticipant> findAllByGameAndStatusIsNot(Game game, ParticipantStatus status, Pageable pageable);

    GameParticipant findByGameAndParticipant(Game game, Team participant);

    Page<GameParticipant> findAllByGameAndStatus(Game game, ParticipantStatus status, Pageable pageable);
    List<GameParticipant> findAllByGameAndStatus(Game game, ParticipantStatus status);

    @Query(nativeQuery = true, value = "select * from game_participants " +
            "left join tournaments_games on game_participants.game_id = tournaments_games.games_id " +
            "left join games g on game_participants.game_id = g.id " +
            "where participant_id = :participantId and game_participants.status <> :status " +
            "and tournaments_games.tournament_id = :tournamentId and stage = :stage and game_id <> :gameId")
    GameParticipant findByParticipantAndStatusIsNotAndTournamentAndGameIsNot(@Param("participantId") Long participantId,
                                                            @Param("status") Integer status,
                                                            @Param("tournamentId") Long tournamentId,
                                                            @Param("stage") Integer stage,
                                                            @Param("gameId") Long gameId);

    @Query("select GP from GameParticipant GP where GP.game.tournament = :tournament " +
            "and GP.participant = :participant and GP.game.stage = :stage and GP.status = :status")
    GameParticipant findByTournamentAndParticipantAndStageAndStatus(@Param("tournament") Tournament tournament,
                                                            @Param("participant") Team participant,
                                                            @Param("stage") Stage stage,
                                                            @Param("status") ParticipantStatus status);
}
