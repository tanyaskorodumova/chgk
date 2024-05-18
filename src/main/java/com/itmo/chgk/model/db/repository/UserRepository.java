package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
