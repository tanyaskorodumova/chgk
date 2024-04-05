package com.itmo.chgk.service;

import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.request.RoundInfoRequest;
import com.itmo.chgk.model.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface GameService {
    Page<GameInfoResponse> getAllGames(Integer page, Integer perPage, String sort, Sort.Direction order);

    GameInfoResponse getGame(Long id);

    GameInfoResponse createGame(GameInfoRequest request);

    GameInfoResponse updateGame(Long id, GameInfoRequest request);

    void deleteGame(Long id);

    Page<ParticipantsInfoResponse> addParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<ParticipantsInfoResponse> approveParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<ParticipantsInfoResponse> deleteParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<ParticipantsInfoResponse> getAllParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<ParticipantsInfoResponse> getParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameQuestionInfoResponse> getGameQuestions(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameQuestionInfoResponse> setGameQuestions(Long id, QuestionPackRequest request, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameQuestionInfoResponse> setGameQuestion(Long gameId, Long questionId, Integer round, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameQuestionInfoResponse> deleteGameQuestion(Long gameId, Long questionId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<RoundInfoResponse> getQuestionsResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameResultInfoResponse> getResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<RoundInfoResponse> getRoundInfo(Long id, Integer round, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<RoundInfoResponse> setRoundResults(Long gameId, Integer round, Long teamId, RoundInfoRequest request, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameResultInfoResponse> countResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameQuestionInfoResponse> startGame(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);
}
