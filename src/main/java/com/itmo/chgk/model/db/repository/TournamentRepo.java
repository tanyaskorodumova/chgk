package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRepo  extends JpaRepository<Tournament, Long> {
    Tournament findFirstByOrganizer(User organizer);
}
