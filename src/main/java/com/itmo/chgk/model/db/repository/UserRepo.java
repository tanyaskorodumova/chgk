package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.UserInfo;
import com.itmo.chgk.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo  extends JpaRepository<UserInfo, Long> {
    Page<UserInfo> findAllByStatusIsNot(Pageable pageable, CommonStatus status);
    Optional<UserInfo> findByEmail(String email);
    Optional<UserInfo> findByLogin(String login);

    @Query("select u from UserInfo u where u.team.id = :teamId")
    Page<UserInfo> findAllByTeam(@Param("teamId") Long teamId, Pageable pageable);

    UserInfo findByLoginAndPassword(String login, String password);

}
