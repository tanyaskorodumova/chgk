package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.UserD;
import com.itmo.chgk.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo  extends JpaRepository<UserD, Long> {
    Page<UserD> findAllByStatusIsNot(Pageable pageable, CommonStatus status);
    Optional<UserD> findByEmail(String email);
    Optional<UserD> findByLogin(String login);

    @Query("select u from UserD u where u.team.id = :teamId")
    Page<UserD> findAllByTeam(@Param("teamId") Long teamId, Pageable pageable);

    UserD findByLoginAndPassword(String login, String password);

}
