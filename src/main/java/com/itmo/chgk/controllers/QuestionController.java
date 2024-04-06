package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.enums.QuestionStatus;
import com.itmo.chgk.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/all")
    public Page<QuestionInfoResponse> getAllQuestions(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer perPage,
                                                      @RequestParam(defaultValue = "id") String sort,
                                                      @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return questionService.getAllQuestions(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    public QuestionInfoResponse getQuestion(@PathVariable Long id) {
        return questionService.getQuestion(id);
    }

    @PutMapping ("/training/package")
    public List<QuestionInfoResponse> getQuestionPack(@RequestBody QuestionPackRequest request) {
        return questionService.getQuestionPack(request);
    }

    @GetMapping("/{id}/answer")
    public QuestionInfoResponse getAnswer(@PathVariable Long id) {
        return questionService.getAnswer(id);
    }

    @PostMapping("/new")
    public QuestionInfoResponse createQuestion(@RequestBody QuestionInfoRequest request) {
        return questionService.createQuestion(request);

//        for (int i = 25001; i <= 30000; i++) {
//            request.setQuestion("Question" + i);
//            request.setAnswer("Answer" + i);
//            request.setSource(Math.random() > 0.25 ? "Source" : null);
//            request.setComplexity(QuestionComplexity.values()[(int) (Math.random() * 5)]);
//            questionService.createQuestion(request);
//        }
//        return null;
    }

    @PutMapping("/{id}")
    public QuestionInfoResponse updateQuestion(@PathVariable Long id, @RequestBody QuestionInfoRequest request) {
        return questionService.updateQuestion(id, request);
    }

    @GetMapping("/approve/get")
    public Page<QuestionInfoResponse> getQuestionsToApprove(@RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer perPage,
                                                            @RequestParam(defaultValue = "id") String sort,
                                                            @RequestParam(defaultValue = "random") Sort.Direction order) {
        return questionService.getQuestionsToApprove(page, perPage, sort, order);
    }

    @PutMapping("/approve/{id}")
    public QuestionInfoResponse approveQuestion(@PathVariable Long id,
                                                @RequestBody QuestionInfoRequest request,
                                                @RequestParam QuestionStatus status) {
        return questionService.approveQuestion(id, request, status);

//        for (int i = 1; i <= 15000; i++) {
//            if (Math.random() > 0.6) {
//                status = QuestionStatus.REJECTED;
//            }
//            else {
//                status = QuestionStatus.APPROVED;
//            }
//            questionService.approveQuestion((long) i, request, status);
//        }
//
//        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

}
