package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.enums.QuestionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    @GetMapping("/all")
    public Page<QuestionInfoResponse> getAllQuestions(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer perPage,
                                                      @RequestParam(defaultValue = "id") String sort,
                                                      @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        //return questionService.getAllCars(page, perPage, sort, order);
        return null;
    }

    @GetMapping("/{id}")
    public QuestionInfoResponse getQuestion(@PathVariable Long id) {
        //return carService.getCar(id);
        return null;
    }

    @GetMapping("/training/package")
    public List<QuestionInfoResponse> getQuestionPack(@RequestBody QuestionPackRequest request) {
        return null;
    }

    @GetMapping("/{id}/answer")
    public QuestionInfoResponse getAnswer(@PathVariable Long id) {
        return null;
    }

    @PostMapping("/new")
    public QuestionInfoResponse createQuestion(@RequestBody QuestionInfoRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public QuestionInfoResponse updateQuestion(@PathVariable Long id, @RequestBody QuestionInfoRequest request) {
        return null;
    }

    @PutMapping("/approve/{id}")
    public QuestionInfoResponse approveQuestion(@PathVariable Long id,
                                                @RequestBody QuestionInfoRequest request,
                                                @RequestBody QuestionStatus status) {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {

    }

}
