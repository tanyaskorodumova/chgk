package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.GameParticipant;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.enums.ParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameParticipantRepo  extends JpaRepository<GameParticipant, Long> {

    @Query(value = "select t from Team t left join GameParticipant p on t = p.participant left join Game g on g = p.game where g.tournament = :tournament and p.status <> 2")
    Page<GameParticipant> findAllByTournament(@Param("tournament") Tournament tournament, Pageable pageable);

    Page<GameParticipant> findAllByGameAndStatusIsNot(Game game, ParticipantStatus status, Pageable pageable);

    GameParticipant findByGameAndParticipant(Game game, Team participant);
}
