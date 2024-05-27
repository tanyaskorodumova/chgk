package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Authority;
import com.itmo.chgk.model.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepo extends JpaRepository<Authority, Long> {
    void deleteAllByUsername(User username);
}
