package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.db.entity.UserD;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepo  extends JpaRepository<Tournament, Long> {
    Tournament findFirstByOrganizer(UserD organizer);
}
