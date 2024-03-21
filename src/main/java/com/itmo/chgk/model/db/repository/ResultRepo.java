package com.itmo.chgk.model.db.repository;

import com.itmo.chgk.model.db.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepo  extends JpaRepository<Result, Long> {
}
