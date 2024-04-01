package com.itmo.chgk.service;

import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
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

    Page<QuestionInfoResponse> getGameQuestions(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<QuestionInfoResponse> setGameQuestions(Long id, QuestionPackRequest request, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<QuestionInfoResponse> setGameQuestion(Long gameId, Long questionId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<QuestionInfoResponse> deleteGameQuestion(Long gameId, Long questionId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<QuestionResultsInfoResponse> getQuestionsResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<GameResultInfoResponse> getResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);
}
