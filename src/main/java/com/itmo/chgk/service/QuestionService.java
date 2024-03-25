package com.itmo.chgk.service;

import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface QuestionService {

    Page<QuestionInfoResponse> getAllQuestions(Integer page, Integer perPage, String sort, Sort.Direction order);

    QuestionInfoResponse getQuestion(Long id);

    List<QuestionInfoResponse> getQuestionPack(QuestionPackRequest request);

    QuestionInfoResponse getAnswer(Long id);

    QuestionInfoResponse createQuestion(QuestionInfoRequest request);

    QuestionInfoResponse updateQuestion(Long id, QuestionInfoRequest request);

    QuestionInfoResponse approveQuestion(Long id, QuestionInfoRequest request, QuestionStatus status);

    void deleteQuestion(Long id);

    Page<QuestionInfoResponse> getQuestionsToApprove(Integer page, Integer perPage, String sort, Sort.Direction order);
}
