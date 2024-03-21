package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepo  extends JpaRepository<Game, Long> {
}
