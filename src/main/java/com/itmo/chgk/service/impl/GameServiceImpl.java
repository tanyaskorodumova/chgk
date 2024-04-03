package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.*;
import com.itmo.chgk.model.db.repository.*;
import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.model.enums.*;
import com.itmo.chgk.service.GameService;
import com.itmo.chgk.service.QuestionService;
import com.itmo.chgk.service.TeamService;
import com.itmo.chgk.service.TournamentService;
import com.itmo.chgk.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameServiceImpl implements GameService {
    private final ObjectMapper mapper;

    private final GameRepo gameRepo;
    private final TournamentRepo tournamentRepo;
    private final GameParticipantRepo gameParticipantRepo;
    private final GameQuestionRepo gameQuestionRepo;
    private final QuestionRepo questionRepo;
    private final QuestionResultRepo questionResultRepo;

    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final QuestionService questionService;

    @Override
    public Page<GameInfoResponse> getAllGames(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<GameInfoResponse> all = gameRepo.findAll(request)
                .getContent()
                .stream()
                .map(game -> {
                    GameInfoResponse gameInfoResponse = mapper.convertValue(game, GameInfoResponse.class);
                    gameInfoResponse.setTournament(mapper.convertValue(game.getTournament(), TournamentInfoResponse.class));
                    return gameInfoResponse;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    public Game getGameDb(Long id) {
        return gameRepo.findById(id).orElseThrow(() -> new CustomException("Игра не найдена", HttpStatus.NOT_FOUND));
    }

    @Override
    public GameInfoResponse getGame(Long id) {
        Game game = getGameDb(id);
        GameInfoResponse response = mapper.convertValue(game, GameInfoResponse.class);
        response.setTournament(mapper.convertValue(game.getTournament(), TournamentInfoResponse.class));
        return response;
    }

    @Override
    public GameInfoResponse createGame(GameInfoRequest request) {
        if (request.getGameName() == null) {
            throw new CustomException("Необходимо ввести название игры", HttpStatus.BAD_REQUEST);
        }
        if (request.getGameDateTime() == null) {
            throw new CustomException("Необходимо ввести дату и время проведения игры", HttpStatus.BAD_REQUEST);
        }
        if (request.getPlace() == null) {
            throw new CustomException("Необходимо ввести место проведения игры", HttpStatus.BAD_REQUEST);
        }
        if (request.getMaxParticipants() == null) {
            throw new CustomException("Необходимо указать максимальное количество участников", HttpStatus.BAD_REQUEST);
        }
        if (request.getTournamentId() == null) {
            throw new CustomException("Турнир должен быть указан", HttpStatus.BAD_REQUEST);
        }
        else {
            Tournament tournament = tournamentService.getTournamentDb(request.getTournamentId());
            if (tournament.getStatus().equals(TournamentStatus.CANCELLED)) {
                throw new CustomException("Турнир отменен", HttpStatus.BAD_REQUEST);
            } else if (tournament.getStatus().equals(TournamentStatus.FINISHED)) {
                throw new CustomException("Турнир завершен", HttpStatus.BAD_REQUEST);
            } else if (tournament.getStatus().equals(TournamentStatus.FINAL)) {
                throw new CustomException("Идет финал, турнир нельзя изменить", HttpStatus.BAD_REQUEST);
            }
        }
        if (request.getStage() == null) {
            throw new CustomException("Необходимо указать стадию турнира", HttpStatus.BAD_REQUEST);
        } else {
            int games;
            games = gameRepo.findAllByTournamentAndStageAndStatusIsNot(tournamentService.getTournamentDb(request.getTournamentId()),
                    request.getStage(),
                    GameStatus.CANCELLED).size();
            if (request.getStage().equals(Stage.QUARTERFINAL) && games >= 4) {
                throw new CustomException("Все чевертьфиналы уже заданы", HttpStatus.CONFLICT);
            } else if (request.getStage().equals(Stage.SEMIFINAL) && games >= 2) {
                throw new CustomException("Все полуфиналы уже заданы", HttpStatus.CONFLICT);
            } else if (request.getStage().equals(Stage.FINAL) && games >= 1) {
                throw new CustomException("Финал уже задан", HttpStatus.CONFLICT);
            }
        }

        Tournament tournament = tournamentService.getTournamentDb(request.getTournamentId());

        Game game = mapper.convertValue(request, Game.class);
        game.setTournament(tournament);
        game.setVacant(game.getMaxParticipants());
        game.setStatus(GameStatus.PLANNED);
        game.setCreatedAt(LocalDateTime.now());

        tournament.setStatus(tournament.getStatus().equals(TournamentStatus.PLANNED) ? TournamentStatus.REGISTRATION : tournament.getStatus());
        tournament = tournamentRepo.save(tournament);

        game = gameRepo.save(game);

        GameInfoResponse response = mapper.convertValue(game, GameInfoResponse.class);
        response.setTournament(mapper.convertValue(tournament, TournamentInfoResponse.class));

        return response;
    }

    @Override
    public GameInfoResponse updateGame(Long id, GameInfoRequest request) {
        Game game = getGameDb(id);

        if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Нельзя внести изменения в начатую игру", HttpStatus.BAD_REQUEST);
        }

        Tournament tournament = game.getTournament();

        if (request.getTournamentId() != null && !game.getTournament().getId().equals(request.getTournamentId())) {
            tournament = tournamentService.getTournamentDb(request.getTournamentId());
            if (tournament.getStatus().equals(TournamentStatus.CANCELLED)) {
                throw new CustomException("Турнир отменен", HttpStatus.BAD_REQUEST);
            } else if (tournament.getStatus().equals(TournamentStatus.FINISHED)) {
                throw new CustomException("Турнир завершен", HttpStatus.BAD_REQUEST);
            } else if (tournament.getStatus().equals(TournamentStatus.FINAL)) {
                throw new CustomException("Идет финал, турнир нельзя изменить", HttpStatus.BAD_REQUEST);
            }

            game.setTournament(tournament);
            tournament.setStatus(tournament.getStatus().equals(TournamentStatus.PLANNED) ? TournamentStatus.REGISTRATION : tournament.getStatus());
            tournament = tournamentRepo.save(tournament);
        }
        if (request.getStage() != null && !game.getStage().equals(request.getStage())) {
            int games;
            games = gameRepo.findAllByTournamentAndStageAndStatusIsNot(tournamentService.getTournamentDb(request.getTournamentId()),
                    request.getStage(),
                    GameStatus.CANCELLED).size();
            if (request.getStage().equals(Stage.QUARTERFINAL) && games >= 4) {
                throw new CustomException("Все чевертьфиналы уже заданы", HttpStatus.CONFLICT);
            } else if (request.getStage().equals(Stage.SEMIFINAL) && games >= 2) {
                throw new CustomException("Все полуфиналы уже заданы", HttpStatus.CONFLICT);
            } else if (request.getStage().equals(Stage.FINAL) && games >= 1) {
                throw new CustomException("Финал уже задан", HttpStatus.CONFLICT);
            }
            game.setStage(request.getStage());
        }

        game.setGameName(request.getGameName() == null ? game.getGameName() : request.getGameName());
        game.setGameDateTime(request.getGameDateTime() == null ? game.getGameDateTime() : request.getGameDateTime());
        game.setPlace(request.getPlace() == null ? game.getPlace() : request.getPlace());
        game.setUpdatedAt(LocalDateTime.now());
        game.setStatus(GameStatus.CHANGED);

        game = gameRepo.save(game);

        GameInfoResponse gameInfoResponse = mapper.convertValue(game, GameInfoResponse.class);
        gameInfoResponse.setTournament(tournamentService.getTournament(tournament.getId()));

        return gameInfoResponse;
    }

    @Override
    public void deleteGame(Long id) {
        Game game = getGameDb(id);

        if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Нельзя отменить начатую игру", HttpStatus.BAD_REQUEST);
        }

        game.setStatus(GameStatus.CANCELLED);
        game.setUpdatedAt(LocalDateTime.now());
        game = gameRepo.save(game);
    }

    @Override
    public Page<ParticipantsInfoResponse> addParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Team team = teamService.getTeamDb(teamId);

        if (game.getStatus().equals(GameStatus.CANCELLED)) {
            throw new CustomException("Игра отменена", HttpStatus.BAD_REQUEST);
        } else if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Игра уже началась", HttpStatus.BAD_REQUEST);
        }

        if (game.getTournament().getMinPoints() > team.getPoints()) {
            throw new CustomException("Недостаточно баллов для регистрации на турнир", HttpStatus.BAD_REQUEST);
        }

        if (!game.getStage().equals(Stage.QUALIFYING)) {
            throw new CustomException("Регистрация открыта только на отборочные игры", HttpStatus.BAD_REQUEST);
        }

        if (game.getVacant() <= 0) {
            throw new CustomException("Нет свободных мест на игру", HttpStatus.BAD_REQUEST);
        }

        if (gameParticipantRepo.findByGameAndParticipant(game, team) != null) {
            throw new CustomException("Команда зарегистрирована на игру ранее", HttpStatus.CONFLICT);
        }

        GameParticipant gameParticipant = new GameParticipant();
        gameParticipant.setParticipant(team);
        gameParticipant.setGame(game);
        gameParticipant.setStatus(ParticipantStatus.REGISTERED);
        gameParticipant = gameParticipantRepo.save(gameParticipant);

        game.setVacant(game.getVacant() - 1);
        game = gameRepo.save(game);

        return getAllParticipants(gameId, page, perPage, sort, order);

    }

    @Override
    public Page<ParticipantsInfoResponse> approveParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Team participant = teamService.getTeamDb(teamId);
        GameParticipant gameParticipant = gameParticipantRepo.findByGameAndParticipant(game, participant);

        if (gameParticipant == null) {
            throw new CustomException("Команда не зарегистрирована", HttpStatus.BAD_REQUEST);
        } else if (gameParticipant.getStatus().equals(ParticipantStatus.APPROVED)) {
            throw new CustomException("Участие подтверждено ранее", HttpStatus.BAD_REQUEST);
        }

        GameParticipant check = gameParticipantRepo.findByParticipantAndStatusIsNotAndTournament(participant,
                ParticipantStatus.REJECTED, game.getTournament(), game.getStage());
        if (check != null) {
            throw new CustomException("Команда уже зарегистрирована на другую игру данного этапа", HttpStatus.BAD_REQUEST);
        }

        gameParticipant.setStatus(ParticipantStatus.APPROVED);
        gameParticipant = gameParticipantRepo.save(gameParticipant);

        return getParticipants(gameId, page, perPage, sort, order);
    }

    @Override
    public Page<ParticipantsInfoResponse> deleteParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Team participant = teamService.getTeamDb(teamId);
        GameParticipant gameParticipant = gameParticipantRepo.findByGameAndParticipant(game, participant);

        if (gameParticipant == null) {
            throw new CustomException("Участник игры не найден", HttpStatus.BAD_REQUEST);
        } else if (gameParticipant.getStatus().equals(ParticipantStatus.REJECTED)) {
            throw new CustomException("Участник отказался от участия ранее", HttpStatus.BAD_REQUEST);
        } else if (gameParticipant.getStatus().equals(ParticipantStatus.APPROVED)) {
            throw new CustomException("Участник подтвердил участие, удалить нельзя", HttpStatus.BAD_REQUEST);
        }

        if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Нельзя отказать от участия в начатой игре", HttpStatus.BAD_REQUEST);
        }

        gameParticipant.setStatus(ParticipantStatus.REJECTED);
        gameParticipant = gameParticipantRepo.save(gameParticipant);

        game.setVacant(game.getVacant()+1);
        game = gameRepo.save(game);

        return getAllParticipants(gameId, page, perPage, sort, order);
    }

    @Override
    public Page<ParticipantsInfoResponse> getAllParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        Game game = getGameDb(id);

        List<ParticipantsInfoResponse> all = gameParticipantRepo.findAllByGameAndStatusIsNot(game, ParticipantStatus.REJECTED, request)
                .getContent()
                .stream()
                .map(gameParticipant -> {
                    TeamInfoResponse teamInfoResponse = teamService.getTeam(gameParticipant.getParticipant().getId());
                    ParticipantStatus status = gameParticipant.getStatus();
                    ParticipantsInfoResponse response = mapper.convertValue(teamInfoResponse, ParticipantsInfoResponse.class);
                    response.setParticipantStatus(status);
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<ParticipantsInfoResponse> getParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        Game game = getGameDb(id);

        List<ParticipantsInfoResponse> all = gameParticipantRepo.findAllByGameAndStatus(game, ParticipantStatus.APPROVED, request)
                .getContent()
                .stream()
                .map(gameParticipant -> {
                    TeamInfoResponse teamInfoResponse = teamService.getTeam(gameParticipant.getParticipant().getId());
                    ParticipantStatus status = gameParticipant.getStatus();
                    ParticipantsInfoResponse response = mapper.convertValue(teamInfoResponse, ParticipantsInfoResponse.class);
                    response.setParticipantStatus(status);
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<GameQuestionInfoResponse> getGameQuestions(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        Game game = getGameDb(id);

        List<GameQuestionInfoResponse> all = gameQuestionRepo.findAllByGame(game, request)
                .getContent()
                .stream()
                .map(gameQuestion -> {
                    QuestionInfoResponse questionInfoResponse = questionService.getQuestion(gameQuestion.getQuestion().getId());
                    GameQuestionInfoResponse response = mapper.convertValue(questionInfoResponse, GameQuestionInfoResponse.class);
                    response.setRound(gameQuestion.getRound());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<GameQuestionInfoResponse> setGameQuestions(Long id, QuestionPackRequest request, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable pageable = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<GameQuestionInfoResponse> finalResponse = null;

        Game game = getGameDb(id);

        if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Игра уже начаалась", HttpStatus.BAD_REQUEST);
        } else if (game.getStatus().equals(GameStatus.CANCELLED)) {
            throw new CustomException("Игра отменена", HttpStatus.BAD_REQUEST);
        }

        AtomicInteger round = new AtomicInteger(0);

        if (!game.getTournament().getLevel().equals(TournamentLevel.FEDERAL)) {
            finalResponse = questionRepo.findGamePack(pageable,
                            game.getId(),
                            request.getMinComplexity() == null ? 0 : request.getMinComplexity().ordinal(),
                            request.getMaxComplexity() == null ? 5 : request.getMaxComplexity().ordinal(),
                            request.getNumber() == null ? 10 : request.getNumber())
                    .getContent()
                    .stream()
                    .map(question -> {
                        GameQuestion gameQuestion = new GameQuestion();
                        gameQuestion.setGame(game);
                        gameQuestion.setQuestion(question);
                        gameQuestion.setRound(round.incrementAndGet());
                        gameQuestion = gameQuestionRepo.save(gameQuestion);
                        QuestionInfoResponse questionInfoResponse = mapper.convertValue(question, QuestionInfoResponse.class);
                        GameQuestionInfoResponse response = mapper.convertValue(questionInfoResponse, GameQuestionInfoResponse.class);
                        response.setRound(gameQuestion.getRound());
                        return response;
                    })
                    .collect(Collectors.toList());
        }
        else {
            finalResponse = questionRepo.findFedGamePack(pageable,
                            request.getMinComplexity() == null ? 0 : request.getMinComplexity().ordinal(),
                            request.getMaxComplexity() == null ? 5 : request.getMaxComplexity().ordinal(),
                            request.getNumber() == null ? 10 : request.getNumber())
                    .getContent()
                    .stream()
                    .map(question -> {
                        GameQuestion gameQuestion = new GameQuestion();
                        gameQuestion.setGame(game);
                        gameQuestion.setQuestion(question);
                        gameQuestion.setRound(round.incrementAndGet());
                        gameQuestion = gameQuestionRepo.save(gameQuestion);
                        QuestionInfoResponse questionInfoResponse = mapper.convertValue(question, QuestionInfoResponse.class);
                        GameQuestionInfoResponse response = mapper.convertValue(questionInfoResponse, GameQuestionInfoResponse.class);
                        response.setRound(gameQuestion.getRound());
                        return response;
                    })
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(finalResponse);
    }

    @Override
    public Page<GameQuestionInfoResponse> setGameQuestion(Long gameId, Long questionId, Integer round, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Question question = questionService.getQuestionDb(questionId);

        if (game.getStatus().equals(GameStatus.FINISHED) ||
                game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Игра уже начаалась", HttpStatus.BAD_REQUEST);
        } else if (game.getStatus().equals(GameStatus.CANCELLED)) {
            throw new CustomException("Игра отменена", HttpStatus.BAD_REQUEST);
        }

        if (!question.getStatus().equals(QuestionStatus.APPROVED)) {
            throw new CustomException("Нельзя использовать данный вопрос: он не одобрен", HttpStatus.BAD_REQUEST);
        }

        if (gameQuestionRepo.findByQuestionAndGameStatusIsNot(question, GameStatus.CANCELLED) != null) {
            throw new CustomException("Вопрос был использован в играх ранее", HttpStatus.BAD_REQUEST);
        }

        GameQuestion gameQuestion = new GameQuestion();
        gameQuestion.setGame(game);
        gameQuestion.setQuestion(question);
        gameQuestion.setRound(round);
        gameQuestion = gameQuestionRepo.save(gameQuestion);

        return getGameQuestions(gameId, page, perPage, sort, order);
    }

    @Override
    public Page<GameQuestionInfoResponse> deleteGameQuestion(Long gameId, Long questionId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Question question = questionService.getQuestionDb(questionId);

        if (game.getStatus().equals(GameStatus.FINISHED) ||
                game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Игра уже начаалась", HttpStatus.BAD_REQUEST);
        } else if (game.getStatus().equals(GameStatus.CANCELLED)) {
            throw new CustomException("Игра отменена", HttpStatus.BAD_REQUEST);
        }

        GameQuestion gameQuestion = gameQuestionRepo.findByGameAndQuestion(game, question);
        if (gameQuestion == null) {
            throw new CustomException("Вопрос не заявлен на игру", HttpStatus.BAD_REQUEST);
        }

        Integer round = gameQuestion.getRound();
        gameQuestionRepo.delete(gameQuestion);

        gameQuestionRepo.findAllByGameAndRoundAfter(game, round)
                .stream()
                .map(gq -> {
                    gq.setRound(gq.getRound()-1);
                    gq = gameQuestionRepo.save(gq);
                    return gq;
                });

        return getGameQuestions(gameId, page, perPage, sort, order);
    }

    @Override
    public Page<QuestionResultsInfoResponse> getQuestionsResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<GameResultInfoResponse> getResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }
}
