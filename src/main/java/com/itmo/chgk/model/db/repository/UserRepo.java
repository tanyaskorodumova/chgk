package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.UserDetail;
import com.itmo.chgk.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo  extends JpaRepository<UserDetail, Long> {
    Page<UserDetail> findAllByStatusIsNot(Pageable pageable, CommonStatus status);
    Optional<UserDetail> findByEmail(String email);
    Optional<UserDetail> findByLogin(String login);

    @Query("select u from UserDetail u where u.team.id = :teamId")
    Page<UserDetail> findAllByTeam(@Param("teamId") Long teamId, Pageable pageable);

    UserDetail findByLoginAndPassword(String login, String password);

}
