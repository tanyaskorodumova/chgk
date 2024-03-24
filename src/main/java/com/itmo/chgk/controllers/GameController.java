package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    @GetMapping("/all")
    public Page<TeamInfoResponse> getAllGames(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer perPage,
                                              @RequestParam(defaultValue = "id") String sort,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        //return questionService.getAllCars(page, perPage, sort, order);
        return null;
    }

    @GetMapping("/{id}")
    public GameInfoResponse getGame(@PathVariable Long id) {
        //return carService.getCar(id);
        return null;
    }

    @PostMapping("/new")
    public GameInfoResponse createGame(@RequestBody GameInfoRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public GameInfoResponse updateGame(@PathVariable Long id, @RequestBody GameInfoRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {

    }

    @PostMapping("/{gameId}/participants/add/{teamId}")
    public Page<TeamInfoResponse> addParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer perPage,
                                                 @RequestParam(defaultValue = "points") String sort,
                                                 @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @PutMapping("/{gameId}/participants/approve/{teamId}")
    public Page<TeamInfoResponse> approveParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "points") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @DeleteMapping("/{gameId}/participants/delete/{teamId}")
    public Page<TeamInfoResponse> deleteParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "points") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/participants/all")
    public Page<TeamInfoResponse> getAllParticipants(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "points") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/participants/approved")
    public Page<TeamInfoResponse> getParticipants(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer perPage,
                                                  @RequestParam(defaultValue = "points") String sort,
                                                  @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/questions")
    public Page<QuestionInfoResponse> getGameQuestions(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @PostMapping("/{id}/questions/setPack")
    public Page<QuestionInfoResponse> setGameQuestions(@PathVariable Long id, @RequestBody QuestionPackRequest request,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @PostMapping("/{gameId}/questions/set/{questionId}")
    public Page<QuestionInfoResponse> setGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @DeleteMapping("/{gameId}/questions/delete/{questionId}")
    public Page<QuestionInfoResponse> deleteGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer perPage,
                                                           @RequestParam(defaultValue = "id") String sort,
                                                           @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/results/questions")
    public Page<QuestionResultsInfoResponse> getQuestionsResults(@PathVariable Long id,
                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer perPage,
                                                                 @RequestParam(defaultValue = "round") String sort,
                                                                 @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/results/final")
    public Page<GameResultInfoResponse> getResults(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer perPage,
                                                   @RequestParam(defaultValue = "place") String sort,
                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

}
