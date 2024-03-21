package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.GameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameParticipantRepo  extends JpaRepository<GameParticipant, Long> {
}
