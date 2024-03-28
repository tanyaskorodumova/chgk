package com.itmo.chgk.model.db.repository;


import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.enums.QuestionComplexity;
import com.itmo.chgk.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestionRepo  extends JpaRepository<Question, Long> {
    Page<Question> findAllByStatus(Pageable pageable, QuestionStatus status);

    @Query(nativeQuery = true, value = "select * from questions where complexity >= coalesce(:minComplexity,0) and complexity <= coalesce(:maxComplexity, 5) and status = 2 order by random() limit coalesce(:number, 10)")
    List<Question> findByQuestionPackRequest(@Param("minComplexity") QuestionComplexity minComplexity,
                                             @Param("maxComplexity") QuestionComplexity maxComplexity,
                                             @Param("number") Integer number);

    @Query(value = "select * from questions where (status = 0 and created_At > (current_date-31)) or (status = 1 and updated_At > (current_date-31))",
            nativeQuery = true)
    Page<Question> findNew(Pageable pageable);
}
