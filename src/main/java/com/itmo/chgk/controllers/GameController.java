package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.request.RoundInfoRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    public GameInfoResponse createGame(@RequestBody @Valid GameInfoRequest request) {
        return gameService.createGame(request);
    }

    @PutMapping("/{id}")
    public GameInfoResponse updateGame(@PathVariable Long id, @RequestBody @Valid GameInfoRequest request) {
        return gameService.updateGame(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
    }

    @PostMapping("/{gameId}/participants/add/{teamId}")
    public Page<ParticipantsInfoResponse> addParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer perPage,
                                                 @RequestParam(defaultValue = "gameId") String sort,
                                                 @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.addParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @PutMapping("/{gameId}/participants/approve/{teamId}")
    public Page<ParticipantsInfoResponse> approveParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.approveParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @DeleteMapping("/{gameId}/participants/delete/{teamId}")
    public Page<ParticipantsInfoResponse> deleteParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.deleteParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants/all")
    public Page<ParticipantsInfoResponse> getAllParticipants(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getAllParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants/approved")
    public Page<ParticipantsInfoResponse> getParticipants(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer perPage,
                                                  @RequestParam(defaultValue = "id") String sort,
                                                  @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/questions")
    public Page<GameQuestionInfoResponse> getGameQuestions(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "round") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getGameQuestions(id, page, perPage, sort, order);
    }

    @PostMapping("/{id}/questions/setPack")
    public List<GameQuestionInfoResponse> setGameQuestions(@PathVariable Long id, @RequestBody QuestionPackRequest request) {
        return gameService.setGameQuestions(id, request);
    }

    @PostMapping("/{gameId}/questions/set/{round}/{questionId}")
    public Page<GameQuestionInfoResponse> setGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId, @PathVariable Integer round,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "round") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.setGameQuestion(gameId, questionId, round, page, perPage, sort, order);
    }

    @DeleteMapping("/{gameId}/questions/delete/{questionId}")
    public Page<GameQuestionInfoResponse> deleteGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer perPage,
                                                           @RequestParam(defaultValue = "round") String sort,
                                                           @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.deleteGameQuestion(gameId, questionId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/rounds/{round}")
    Page<RoundInfoResponse> getRoundInfo(@PathVariable Long id, @PathVariable Integer round,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer perPage,
                                         @RequestParam(defaultValue = "teamId") String sort,
                                         @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getRoundInfo(id, round,page, perPage,sort, order);
    }

    @PostMapping("/{gameId}/rounds/{round}/{teamId}")
    Page<RoundInfoResponse> setRoundResults(@PathVariable Long gameId, @PathVariable Integer round,
                                            @PathVariable Long teamId, @RequestBody RoundInfoRequest request,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage,
                                            @RequestParam(defaultValue = "id") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.setRoundResults(gameId, round, teamId, request, page, perPage, sort, order);
    }


    @GetMapping("/{id}/results/questions")
    public Page<RoundInfoResponse> getQuestionsResults(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getQuestionsResults(id, page, perPage, sort, order);
    }

    @PutMapping("/{id}/results/count")
    public Page<GameResultInfoResponse> countResults(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "place") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.countResults(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/results/final")
    public Page<GameResultInfoResponse> getResults(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer perPage,
                                                   @RequestParam(defaultValue = "place") String sort,
                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getResults(id, page, perPage, sort, order);
    }

    @PutMapping("/{id}/start")
    public Page<GameQuestionInfoResponse> startGame(@PathVariable Long id,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer perPage,
                          @RequestParam(defaultValue = "round") String sort,
                          @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.startGame(id, page, perPage, sort, order);
    }

}
