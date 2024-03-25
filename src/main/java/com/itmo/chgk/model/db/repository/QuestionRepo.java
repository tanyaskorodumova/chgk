package com.itmo.chgk.model.db.repository;


import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.enums.QuestionComplexity;
import com.itmo.chgk.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepo  extends JpaRepository<Question, Long> {
    Page<Question> findAllByStatus(Pageable pageable, QuestionStatus status);

    @Query(nativeQuery = true, value = "select * from Question where complexity.ordinal() between coalesce(:minComplexity,0) and coalesce(:maxComplexity, 5) limit coalesce(:number, 10)")
    List<Question> findByComplexityBetween(@Param("minComplexity") Integer minComplexity,
                                             @Param("maxComplexity") Integer maxComplexity,
                                             @Param("number") Integer number);

    @Query("select q from Question q where 1=1 and ((q.status = 0 and q.createdAt > (current_date-31)) or (q.status = 1 and q.updatedAt > (current_date-31)))")
    Page<Question> findNew(Pageable pageable);
}
