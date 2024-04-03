package com.itmo.chgk.service;

import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface QuestionService {

    Page<QuestionInfoResponse> getAllQuestions(Integer page, Integer perPage, String sort, Sort.Direction order);

    QuestionInfoResponse getQuestion(Long id);

    Page<QuestionInfoResponse> getQuestionPack(QuestionPackRequest request, Integer page, Integer perPage, String sort, Sort.Direction order);

    QuestionInfoResponse getAnswer(Long id);

    QuestionInfoResponse createQuestion(QuestionInfoRequest request);

    QuestionInfoResponse updateQuestion(Long id, QuestionInfoRequest request);

    QuestionInfoResponse approveQuestion(Long id, QuestionInfoRequest request, QuestionStatus status);

    void deleteQuestion(Long id);

    Page<QuestionInfoResponse> getQuestionsToApprove(Integer page, Integer perPage, String sort, Sort.Direction order);

    Question getQuestionDb(Long id);
}
