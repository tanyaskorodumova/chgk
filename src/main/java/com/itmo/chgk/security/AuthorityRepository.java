package com.itmo.chgk.security;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
    void deleteAllByUsername(User username);
}
