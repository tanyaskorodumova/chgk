package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Authority;
import com.itmo.chgk.model.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
    void deleteAllByUsername(User username);
}
