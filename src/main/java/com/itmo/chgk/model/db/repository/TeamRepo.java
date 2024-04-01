package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.GameParticipant;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepo extends JpaRepository<Team, Long> {
    Page<Team> findAllByStatusIsNot(Pageable pageable, CommonStatus status);

}
