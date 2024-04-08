package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.enums.QuestionStatus;
import com.itmo.chgk.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Получение информации обо всех одобренных вопрсах из базы")
    public Page<QuestionInfoResponse> getAllQuestions(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer perPage,
                                                      @RequestParam(defaultValue = "id") String sort,
                                                      @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return questionService.getAllQuestions(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о конкретном вопросе")
    public QuestionInfoResponse getQuestion(@PathVariable Long id) {
        return questionService.getQuestion(id);
    }

    @PutMapping ("/training/package")
    @Operation(summary = "Получение тренировочного пакета вопросов из ранее использовавшихся в состоявшихся играх по заданным критериям")
    public List<QuestionInfoResponse> getQuestionPack(@RequestBody QuestionPackRequest request) {
        return questionService.getQuestionPack(request);
    }

    @GetMapping("/{id}/answer")
    @Operation(summary = "Получение полной информации о вопросе с ответом и ссылкой на источник (если есть)")
    public QuestionInfoResponse getAnswer(@PathVariable Long id) {
        return questionService.getAnswer(id);
    }

    @PostMapping("/new")
    @Operation(summary = "Создание нового вопроса")
    public QuestionInfoResponse createQuestion(@RequestBody QuestionInfoRequest request) {
        return questionService.createQuestion(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактирование вопроса")
    public QuestionInfoResponse updateQuestion(@PathVariable Long id, @RequestBody QuestionInfoRequest request) {
        return questionService.updateQuestion(id, request);
    }

    @GetMapping("/approve/get")
    @Operation(summary = "Получение вопросов, добавленных или измененных за последние 30 дней, нуждающихся в визе администратора")
    public Page<QuestionInfoResponse> getQuestionsToApprove(@RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer perPage,
                                                            @RequestParam(defaultValue = "id") String sort,
                                                            @RequestParam(defaultValue = "random") Sort.Direction order) {
        return questionService.getQuestionsToApprove(page, perPage, sort, order);
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "Одобрение вопроса")
    public QuestionInfoResponse approveQuestion(@PathVariable Long id,
                                                @RequestBody QuestionInfoRequest request,
                                                @RequestParam QuestionStatus status) {
        return questionService.approveQuestion(id, request, status);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление вопроса")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

}
