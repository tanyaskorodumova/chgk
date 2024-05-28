package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.entity.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserInfoRepo extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByEmail(String email);

    Optional<UserInfo> findByLogin(User user);

    @Query("select u from UserInfo u where u.team.id = :teamId")
    Page<UserInfo> findAllByTeam(@Param("teamId") Long teamId, Pageable pageable);
}
