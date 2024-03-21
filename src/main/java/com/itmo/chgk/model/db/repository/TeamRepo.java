package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepo extends JpaRepository<Team, Long> {

}
