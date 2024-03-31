package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.db.entity.TournamentTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentTableRepo extends JpaRepository<TournamentTable, Long> {
    List<TournamentTable> findAllByTournament(Tournament tournament);
}
