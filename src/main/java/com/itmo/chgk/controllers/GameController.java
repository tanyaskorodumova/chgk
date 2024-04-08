package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.request.RoundInfoRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Получение информации обо всех играх")
    public Page<GameInfoResponse> getAllGames(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer perPage,
                                              @RequestParam(defaultValue = "id") String sort,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return gameService.getAllGames(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о конкретной игре")
    public GameInfoResponse getGame(@PathVariable Long id) {
        return gameService.getGame(id);
    }

    @PostMapping("/new")
    @Operation(summary = "Создание новой игры")
    public GameInfoResponse createGame(@RequestBody @Valid GameInfoRequest request) {
        return gameService.createGame(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактирование игры")
    public GameInfoResponse updateGame(@PathVariable Long id, @RequestBody @Valid GameInfoRequest request) {
        return gameService.updateGame(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление игры")
    public void deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
    }

    @PostMapping("/{gameId}/participants/add/{teamId}")
    @Operation(summary = "Добавление команды-участника игры")
    public Page<ParticipantsInfoResponse> addParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer perPage,
                                                 @RequestParam(defaultValue = "gameId") String sort,
                                                 @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.addParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @PutMapping("/{gameId}/participants/approve/{teamId}")
    @Operation(summary = "Подтверждение участия команды в игре")
    public Page<ParticipantsInfoResponse> approveParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.approveParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @DeleteMapping("/{gameId}/participants/delete/{teamId}")
    @Operation(summary = "Удаление участника/отказ от участия в игре")
    public Page<ParticipantsInfoResponse> deleteParticipant(@PathVariable Long gameId, @PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.deleteParticipant(gameId, teamId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants/all")
    @Operation(summary = "Получение информации обо всех командах, зарегистрированных на игру (без отменивших участие)")
    public Page<ParticipantsInfoResponse> getAllParticipants(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getAllParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants/approved")
    @Operation(summary = "Получение информации о командах, подтвердивших участие в игре")
    public Page<ParticipantsInfoResponse> getParticipants(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer perPage,
                                                  @RequestParam(defaultValue = "id") String sort,
                                                  @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/questions")
    @Operation(summary = "Получение информации о вопросах на игру")
    public Page<GameQuestionInfoResponse> getGameQuestions(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "round") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getGameQuestions(id, page, perPage, sort, order);
    }

    @PostMapping("/{id}/questions/setPack")
    @Operation(summary = "Автоподбор пакета вопросов для игры по заданным критериям и уровню турнира")
    public List<GameQuestionInfoResponse> setGameQuestions(@PathVariable Long id, @RequestBody QuestionPackRequest request) {
        return gameService.setGameQuestions(id, request);
    }

    @PostMapping("/{gameId}/questions/set/{round}/{questionId}")
    @Operation(summary = "Ручное добавление вопроса в список вопросов на игру")
    public Page<GameQuestionInfoResponse> setGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId, @PathVariable Integer round,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "round") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.setGameQuestion(gameId, questionId, round, page, perPage, sort, order);
    }

    @DeleteMapping("/{gameId}/questions/delete/{questionId}")
    @Operation(summary = "Удаление вопроса из списка вопросов на игру")
    public Page<GameQuestionInfoResponse> deleteGameQuestion(@PathVariable Long gameId, @PathVariable Long questionId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer perPage,
                                                           @RequestParam(defaultValue = "round") String sort,
                                                           @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.deleteGameQuestion(gameId, questionId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/rounds/{round}")
    @Operation(summary = "Получение информации о вопросе и ответах участников в конкретном раунде игры")
    Page<RoundInfoResponse> getRoundInfo(@PathVariable Long id, @PathVariable Integer round,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer perPage,
                                         @RequestParam(defaultValue = "teamId") String sort,
                                         @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getRoundInfo(id, round,page, perPage,sort, order);
    }

    @PostMapping("/{gameId}/rounds/{round}/{teamId}")
    @Operation(summary = "Установка результата ответа конкретной команды в конкретном раунде игры")
    Page<RoundInfoResponse> setRoundResults(@PathVariable Long gameId, @PathVariable Integer round,
                                            @PathVariable Long teamId, @RequestBody RoundInfoRequest request,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage,
                                            @RequestParam(defaultValue = "id") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.setRoundResults(gameId, round, teamId, request, page, perPage, sort, order);
    }

    @GetMapping("/{id}/results/questions")
    @Operation(summary = "Получение информации об ответах участников по всем раундам игры")
    public Page<RoundInfoResponse> getQuestionsResults(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer perPage,
                                                       @RequestParam(defaultValue = "id") String sort,
                                                       @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getQuestionsResults(id, page, perPage, sort, order);
    }

    @PutMapping("/{id}/results/count")
    @Operation(summary = "Расчет итоговых баллов и мест участников игры на основе данных об их ответах")
    public Page<GameResultInfoResponse> countResults(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "place") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.countResults(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/results/final")
    @Operation(summary = "Получение итогов игры")
    public Page<GameResultInfoResponse> getResults(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer perPage,
                                                   @RequestParam(defaultValue = "place") String sort,
                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.getResults(id, page, perPage, sort, order);
    }

    @PutMapping("/{id}/start")
    @Operation(summary = "Начало игры, создание таблиц ответов по раундам и шаблона итоговой таблицы")
    public Page<GameQuestionInfoResponse> startGame(@PathVariable Long id,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer perPage,
                          @RequestParam(defaultValue = "round") String sort,
                          @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return gameService.startGame(id, page, perPage, sort, order);
    }

}
