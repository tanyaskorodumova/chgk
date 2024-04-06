package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo  extends JpaRepository<User, Long> {
    Page<User> findAllByStatusIsNot(Pageable pageable, CommonStatus status);
    Optional<User> findByEmail(String email);
    Optional<User> findByLogin(String login);

    @Query("select u from User u where u.team.id = :teamId")
    Page<User> findAllByTeam(@Param("teamId") Long teamId, Pageable pageable);

}
