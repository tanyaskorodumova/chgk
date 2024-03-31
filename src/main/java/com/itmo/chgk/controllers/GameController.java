package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping("/all")
    public Page<GameInfoResponse> getAllGames(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer perPage,
                                              @RequestParam(defaultValue = "id") String sort,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return gameService.getAllGames(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    public GameInfoResponse getGame(@PathVariable Long id) {
        return gameService.getGame(id);
    }

    @PostMapping("/new")
    public GameInfoResponse createGame(@RequestBody GameInfoRequest request) {
        return gameService.createGame(request);
    }

    @PutMapping("/{id}")
    public GameInfoResponse updateGame(@PathVariable Long id, @RequestBody GameInfoRequest request) {
        return gameService.updateGame(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
    }

    @PostMapping("/{gameId}/participants/add/{teamId}")
    public Page<TeamInfoResponse> addParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer perPage,
                                                 @RequestParam(defaultValue = "points") String sort,
                                                 @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.addParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @PutMapping("/{gameId}/participants/approve/{teamId}")
    public Page<TeamInfoResponse> approveParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "points") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.approveParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @DeleteMapping("/{gameId}/participants/delete/{teamId}")
    public Page<TeamInfoResponse> deleteParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "points") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.deleteParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants/all")
    public Page<TeamInfoResponse> getAllParticipants(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "points") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getAllParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants/approved")
    public Page<TeamInfoResponse> getParticipants(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer perPage,
                                                  @RequestParam(defaultValue = "points") String sort,
                                                  @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/questions")
    public Page<QuestionInfoResponse> getGameQuestions(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getGameQuestions(id, page, perPage, sort, order);
    }

    @PostMapping("/{id}/questions/setPack")
    public Page<QuestionInfoResponse> setGameQuestions(@PathVariable Long id, @RequestBody QuestionPackRequest request,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.setGameQuestions(id, request, page, perPage, sort, order);
    }

    @PostMapping("/{gameId}/questions/set/{questionId}")
    public Page<QuestionInfoResponse> setGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.setGameQuestion(gameId, questionId, page, perPage, sort, order);
    }

    @DeleteMapping("/{gameId}/questions/delete/{questionId}")
    public Page<QuestionInfoResponse> deleteGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer perPage,
                                                           @RequestParam(defaultValue = "id") String sort,
                                                           @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.deleteGameQuestion(gameId, questionId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/results/questions")
    public Page<QuestionResultsInfoResponse> getQuestionsResults(@PathVariable Long id,
                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer perPage,
                                                                 @RequestParam(defaultValue = "round") String sort,
                                                                 @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getQuestionsResults(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/results/final")
    public Page<GameResultInfoResponse> getResults(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer perPage,
                                                   @RequestParam(defaultValue = "place") String sort,
                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getResults(id, page, perPage, sort, order);
    }

}
