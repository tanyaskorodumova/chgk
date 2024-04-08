package com.itmo.chgk.job;

import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.enums.QuestionStatus;
import com.itmo.chgk.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobService {
    private final QuestionService questionService;

    @Scheduled(cron = "0 0 0 * * *")
    public void rejectOldQuestions() {
        questionService.getQuestionsToDelete()
                .stream()
                .forEach(question ->
                        questionService.approveQuestion(question.getId(), new QuestionInfoRequest(), QuestionStatus.REJECTED));
    }
}
