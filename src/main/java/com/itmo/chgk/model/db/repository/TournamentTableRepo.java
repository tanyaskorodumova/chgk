package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.TournamentTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentTableRepo extends JpaRepository<TournamentTable, Long> {
}
