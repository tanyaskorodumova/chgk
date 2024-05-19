package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.*;
import com.itmo.chgk.model.db.repository.*;
import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.request.RoundInfoRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.model.enums.*;
import com.itmo.chgk.service.*;
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
import java.util.Comparator;
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
    private final ResultRepo resultRepo;
    private final TeamRepo teamRepo;

    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final QuestionService questionService;
//    private final LoggedUserManagementService loggedUserManagementService;

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
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN)) {
//            throw new CustomException("У пользователя нет доступа к созданию игры", HttpStatus.FORBIDDEN);
//        }

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
//            if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                    !tournament.getId().equals(loggedUserManagementService.getTournamentId())) {
//                throw new CustomException("У пользователя нет прав на создание игры данного турнира", HttpStatus.FORBIDDEN);
//            }

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
        game = gameRepo.save(game);

        tournament.getGames().add(game);
        tournament.setStatus(tournament.getStatus().equals(TournamentStatus.PLANNED) ? TournamentStatus.REGISTRATION : tournament.getStatus());
        tournament = tournamentRepo.save(tournament);

        GameInfoResponse response = mapper.convertValue(game, GameInfoResponse.class);
        response.setTournament(mapper.convertValue(tournament, TournamentInfoResponse.class));

        return response;
    }

    @Override
    public GameInfoResponse updateGame(Long id, GameInfoRequest request) {
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN)) {
//            throw new CustomException("У пользователя нет доступа к редактированию игры", HttpStatus.FORBIDDEN);
//        }

        Game game = getGameDb(id);

        if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Нельзя внести изменения в начатую игру", HttpStatus.BAD_REQUEST);
        }

        Tournament tournament = game.getTournament();
//        if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !tournament.getId().equals(loggedUserManagementService.getTournamentId())) {
//            throw new CustomException("У пользователя нет прав на редактирование данной игры", HttpStatus.FORBIDDEN);
//        }

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
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN)) {
//            throw new CustomException("У пользователя нет доступа к редактированию игры", HttpStatus.FORBIDDEN);
//        }

        Game game = getGameDb(id);

//        if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//            !game.getTournament().getId().equals(loggedUserManagementService.getTournamentId())) {
//            throw new CustomException("У пользователя нет прав на удаление данной игры", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Нельзя отменить начатую игру", HttpStatus.BAD_REQUEST);
        }

        game.setStatus(GameStatus.CANCELLED);
        game.setUpdatedAt(LocalDateTime.now());
        gameRepo.save(game);
    }

    @Override
    public Page<ParticipantsInfoResponse> addParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Team team = teamService.getTeamDb(teamId);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.CAPTAIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.VICECAPTAIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN)) {
//            throw new CustomException("У пользователя нет прав на регистрацию команды на игру", HttpStatus.FORBIDDEN);
//        } else if ((loggedUserManagementService.getUserInfo().getRole().equals(UserRole.CAPTAIN) ||
//                    loggedUserManagementService.getUserInfo().getRole().equals(UserRole.VICECAPTAIN)) &&
//                    !loggedUserManagementService.getTeamId().equals(team.getId())) {
//            throw new CustomException("У пользователя нет прав на регистрацию данной команды на игру", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.CANCELLED)) {
            throw new CustomException("Игра отменена", HttpStatus.BAD_REQUEST);
        } else if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Игра уже началась", HttpStatus.BAD_REQUEST);
        }

        if ((team.getPoints() == null && game.getTournament().getMinPoints() != null && game.getTournament().getMinPoints() != 0) ||
                (team.getPoints() != null && game.getTournament().getMinPoints() != null && game.getTournament().getMinPoints() > team.getPoints())) {
            throw new CustomException("Недостаточно баллов для регистрации на турнир", HttpStatus.BAD_REQUEST);
        }

        if (!game.getStage().equals(Stage.QUALIFYING)) {
            List<Game> prevStageGames = gameRepo.findAllByTournamentAndStageAndStatusIsNot(game.getTournament(),
                    Stage.values()[game.getStage().ordinal() - 1], GameStatus.CANCELLED);

            if (!prevStageGames.stream().allMatch(psg -> psg.getStatus().equals(GameStatus.FINISHED))) {
                throw new CustomException("Игры предыдущей стадии еще не завершены", HttpStatus.BAD_REQUEST);
            }

            if (gameParticipantRepo.findByTournamentAndParticipantAndStageAndStatus(game.getTournament(),
                    team, Stage.values()[game.getStage().ordinal() - 1], ParticipantStatus.APPROVED) == null) {
                throw new CustomException("Команда не принимала участия в играх предыдущего этапа", HttpStatus.BAD_REQUEST);
            }
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
        gameParticipantRepo.save(gameParticipant);

        game.setVacant(game.getVacant() - 1);
        game = gameRepo.save(game);

        return getAllParticipants(game.getId(), page, perPage, sort, order);

    }

    @Override
    public Page<ParticipantsInfoResponse> approveParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Team participant = teamService.getTeamDb(teamId);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.CAPTAIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.VICECAPTAIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN)) {
//            throw new CustomException("У пользователя нет прав на подтверждение участия команды в игре", HttpStatus.FORBIDDEN);
//        } else if ((loggedUserManagementService.getUserInfo().getRole().equals(UserRole.CAPTAIN) ||
//                loggedUserManagementService.getUserInfo().getRole().equals(UserRole.VICECAPTAIN)) &&
//                !loggedUserManagementService.getTeamId().equals(participant.getId())) {
//            throw new CustomException("У пользователя нет прав на регистрацию данной команды на игру", HttpStatus.FORBIDDEN);
//        }

        GameParticipant gameParticipant = gameParticipantRepo.findByGameAndParticipant(game, participant);

        if (gameParticipant == null) {
            throw new CustomException("Команда не зарегистрирована", HttpStatus.BAD_REQUEST);
        } else if (gameParticipant.getStatus().equals(ParticipantStatus.APPROVED)) {
            throw new CustomException("Участие подтверждено ранее", HttpStatus.BAD_REQUEST);
        }

        GameParticipant check = gameParticipantRepo.findByParticipantAndStatusIsNotAndTournamentAndGameIsNot(participant.getId(),
                ParticipantStatus.REJECTED.ordinal(), game.getTournament().getId(), game.getStage().ordinal(), game.getId());
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

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.CAPTAIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.VICECAPTAIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на отмену участия команды в игре", HttpStatus.FORBIDDEN);
//        } else if ((loggedUserManagementService.getUserInfo().getRole().equals(UserRole.CAPTAIN) ||
//                loggedUserManagementService.getUserInfo().getRole().equals(UserRole.VICECAPTAIN)) &&
//                !loggedUserManagementService.getTeamId().equals(participant.getId())) {
//            throw new CustomException("У пользователя нет прав на отмену участия данной команды в игре", HttpStatus.FORBIDDEN);
//        }

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
                    response.setGameId(game.getId());
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
                    response.setGameId(game.getId());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<GameQuestionInfoResponse> getGameQuestions(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        Game game = getGameDb(id);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на получение вопросов к игре", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на получение вопросов к данной игре", HttpStatus.FORBIDDEN);
//        }

        List<GameQuestionInfoResponse> all = gameQuestionRepo.findAllByGame(game, request)
                .getContent()
                .stream()
                .map(gameQuestion -> {
                    QuestionInfoResponse questionInfoResponse = mapper.convertValue(gameQuestion.getQuestion(), QuestionInfoResponse.class);
                    GameQuestionInfoResponse response = mapper.convertValue(questionInfoResponse, GameQuestionInfoResponse.class);
                    response.setRound(gameQuestion.getRound());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public List<GameQuestionInfoResponse> setGameQuestions(Long id, QuestionPackRequest request) {
        List<GameQuestionInfoResponse> finalResponse = null;

        Game game = getGameDb(id);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на установку вопросов к игре", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на установку вопросов к данной игре", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.FINISHED) ||
            game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Игра уже начаалась", HttpStatus.BAD_REQUEST);
        } else if (game.getStatus().equals(GameStatus.CANCELLED)) {
            throw new CustomException("Игра отменена", HttpStatus.BAD_REQUEST);
        }

        AtomicInteger round = new AtomicInteger(0);

        if (!game.getTournament().getLevel().equals(TournamentLevel.FEDERAL)) {
            finalResponse = questionRepo.findGamePack(game.getId(),
                            request.getMinComplexity() == null ? 0 : request.getMinComplexity().ordinal(),
                            request.getMaxComplexity() == null ? 5 : request.getMaxComplexity().ordinal(),
                            request.getNumber() == null ? 10 : request.getNumber())
                    .stream()
                    .sorted(Comparator.comparing(Question::getComplexity))
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
            finalResponse = questionRepo.findFedGamePack(request.getMinComplexity() == null ? 0 : request.getMinComplexity().ordinal(),
                            request.getMaxComplexity() == null ? 5 : request.getMaxComplexity().ordinal(),
                            request.getNumber() == null ? 10 : request.getNumber())
                    .stream()
                    .sorted(Comparator.comparing(Question::getComplexity))
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

        return finalResponse;
    }

    @Override
    public Page<GameQuestionInfoResponse> setGameQuestion(Long gameId, Long questionId, Integer round, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Question question = questionService.getQuestionDb(questionId);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на установку вопросов к игре", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на установку вопросов к данной игре", HttpStatus.FORBIDDEN);
//        }

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
        gameQuestionRepo.save(gameQuestion);

        return getGameQuestions(game.getId(), page, perPage, sort, order);
    }

    @Override
    public Page<GameQuestionInfoResponse> deleteGameQuestion(Long gameId, Long questionId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);
        Question question = questionService.getQuestionDb(questionId);
//
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на удаление вопросов к игре", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на удаление вопросов к данной игре", HttpStatus.FORBIDDEN);
//        }

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
                .forEach(gq -> {
                    gq.setRound(gq.getRound()-1);
                    gameQuestionRepo.save(gq);
                });

        return getGameQuestions(gameId, page, perPage, sort, order);
    }

    @Override
    public Page<RoundInfoResponse> getRoundInfo(Long id, Integer round, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable pageable = PaginationUtil.getPageRequest(page, perPage, sort, order);

        Game game = getGameDb(id);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на получение информации о вопросах игры", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на получение информации о вопросах данной игры", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.CANCELLED) ||
                game.getStatus().equals(GameStatus.PLANNED) ||
                game.getStatus().equals(GameStatus.CHANGED)) {
            throw new CustomException("Игра еще не состоялась", HttpStatus.BAD_REQUEST);
        }

        GameQuestion gameQuestion = gameQuestionRepo.findByGameAndRound(game, round);
        if (gameQuestion == null) {
            throw new CustomException("Раунд игры не найден", HttpStatus.BAD_REQUEST);
        }

        List<RoundInfoResponse> roundInfoResponses = questionResultRepo.findAllByQuestion(gameQuestion, pageable)
                .getContent()
                .stream()
                .map(questionResult -> {
                    RoundInfoResponse response = new RoundInfoResponse();
                    response.setGameId(game.getId());
                    response.setRound(round);
                    response.setQuestionId(questionResult.getQuestion().getQuestion().getId());
                    response.setQuestion(questionResult.getQuestion().getQuestion().getQuestion());
                    response.setTeamId(questionResult.getTeam().getId());
                    response.setTeamName(questionResult.getTeam().getTeamName());
                    response.setIsCorrect(questionResult.getIsCorrect());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(roundInfoResponses);

    }

    @Override
    public Page<RoundInfoResponse> setRoundResults(Long gameId, Integer round, Long teamId, RoundInfoRequest request, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(gameId);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на установку результатов игры", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на установку результатов данной игры", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.CANCELLED) ||
                game.getStatus().equals(GameStatus.PLANNED) ||
                game.getStatus().equals(GameStatus.CHANGED)) {
            throw new CustomException("Игра еще не состоялась", HttpStatus.BAD_REQUEST);
        }

        GameQuestion gameQuestion = gameQuestionRepo.findByGameAndRound(game, round);
        if (gameQuestion == null) {
            throw new CustomException("Раунд игры не найден", HttpStatus.BAD_REQUEST);
        }

        Team team = teamService.getTeamDb(teamId);
        GameParticipant gameParticipant = gameParticipantRepo.findByGameAndParticipant(game, team);
        if (gameParticipant == null) {
            throw new CustomException("Команда не является участником игры", HttpStatus.BAD_REQUEST);
        }

        QuestionResult questionResult = questionResultRepo.findByQuestionAndTeam(gameQuestion.getId(), team.getId());
        questionResult.setIsCorrect(request.getIsCorrect());
        questionResult.setUpdatedAt(LocalDateTime.now());
        questionResultRepo.save(questionResult);

        Result gameResult = resultRepo.findByGameAndTeam(game, team);
        gameResult.setPoints(request.getIsCorrect() ?
                (gameResult.getPoints() == null ? 1 : gameResult.getPoints() + 1)
                : (gameResult.getPoints() == null ? 0 : gameResult.getPoints()));
        gameResult = resultRepo.save(gameResult);

        team.setAnswers(team.getAnswers() == null ? 1 : team.getAnswers() + 1);
        team.setCorrectAnswers(request.getIsCorrect() ?
                (team.getCorrectAnswers() == null ? 1 : team.getCorrectAnswers() + 1)
                : (team.getCorrectAnswers() == null ? 0 : team.getCorrectAnswers()));
        team.setCorrectAnswersPct(team.getCorrectAnswers() == null ? 0
                : (double) team.getCorrectAnswers() / (double) team.getAnswers() * 100);
        team = teamRepo.save(team);

        return getRoundInfo(gameId, round, page, perPage, sort, order);
    }

    @Override
    public Page<RoundInfoResponse> getQuestionsResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(id);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на получение информации о вопросах игры", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на получение информации о вопросах данной игры", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.CANCELLED) ||
                game.getStatus().equals(GameStatus.PLANNED) ||
                game.getStatus().equals(GameStatus.CHANGED)) {
            throw new CustomException("Игра еще не состоялась", HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<RoundInfoResponse> roundInfoResponses = questionResultRepo.findAllByGameId(game.getId(), pageable)
                .getContent()
                .stream()
                .map(questionResult -> {
                    RoundInfoResponse response = new RoundInfoResponse();
                    response.setGameId(game.getId());
                    response.setRound(questionResult.getQuestion().getRound());
                    response.setQuestionId(questionResult.getQuestion().getQuestion().getId());
                    response.setQuestion(questionResult.getQuestion().getQuestion().getQuestion());
                    response.setTeamId(questionResult.getTeam().getId());
                    response.setTeamName(questionResult.getTeam().getTeamName());
                    response.setIsCorrect(questionResult.getIsCorrect());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(roundInfoResponses);
    }

    @Override
    public Page<GameResultInfoResponse> getResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(id);
        if (game.getStatus().equals(GameStatus.CANCELLED) ||
                game.getStatus().equals(GameStatus.PLANNED) ||
                game.getStatus().equals(GameStatus.CHANGED)) {
            throw new CustomException("Игра еще не состоялась", HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PaginationUtil.getPageRequest(page, perPage, sort, order);

        AtomicInteger place = new AtomicInteger(0);

        List<GameResultInfoResponse> all = resultRepo.findAllByGame(game, pageable)
                .getContent()
                .stream()
                .sorted(Comparator.comparing(Result::getPoints).reversed())
                .map(result -> {
                    result.setPlace(place.incrementAndGet());
                    result = resultRepo.save(result);
                    GameResultInfoResponse response = new GameResultInfoResponse();
                    response.setPlace(result.getPlace());
                    response.setTeamId(result.getTeam().getId());
                    response.setTeamName(result.getTeam().getTeamName());
                    response.setPoints(result.getPoints());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<GameResultInfoResponse> countResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(id);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на управление игрой", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на управление данной игрой", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.FINISHED)) {
            throw new CustomException("Результаты посчитаны ранее", HttpStatus.CONFLICT);
        } else if (!game.getStatus().equals(GameStatus.ONGOING)) {
            throw new CustomException("Игра не началась", HttpStatus.BAD_REQUEST);
        }

        boolean isNotFinished = questionResultRepo.findAllByGameId(game.getId())
                .stream()
                .anyMatch(questionResult -> questionResult.getIsCorrect() == null);
        if (isNotFinished) {
            throw new CustomException("Игра еще не завершена или внесены не все результаты", HttpStatus.CONFLICT);
        }

        game.setStatus(GameStatus.FINISHED);

        Page<GameResultInfoResponse> gameResultInfoResponses = getResults(id, page, perPage, sort, order);

        if (game.getStage().equals(Stage.FINAL)) {
            tournamentService.countResults(game.getTournament().getId());
        }

        return gameResultInfoResponses;
    }

    @Override
    public Page<GameQuestionInfoResponse> startGame(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Game game = getGameDb(id);

//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER)) {
//            throw new CustomException("У пользователя нет прав на управление игрой", HttpStatus.FORBIDDEN);
//        } else if (loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ORGANIZER) &&
//                !loggedUserManagementService.getTournamentId().equals(game.getTournament().getId())){
//            throw new CustomException("У пользователя нет прав на управление данной игрой", HttpStatus.FORBIDDEN);
//        }

        if (game.getStatus().equals(GameStatus.CANCELLED)) {
            throw new CustomException("Игра была отменена", HttpStatus.BAD_REQUEST);
        }

        game.setStatus(GameStatus.ONGOING);
        game = gameRepo.save(game);

        Tournament tournament = game.getTournament();
        switch (game.getStage()) {
            case QUALIFYING:
                tournament.setStatus(TournamentStatus.QUALIFYING);
                break;
            case QUARTERFINAL:
                tournament.setStatus(TournamentStatus.QUARTERFINALS);
                break;
            case SEMIFINAL:
                tournament.setStatus(TournamentStatus.SEMIFINALS);
                break;
            case FINAL:
                tournament.setStatus(TournamentStatus.FINAL);
                break;
        }
        tournamentRepo.save(tournament);

        List<GameParticipant> gameParticipants = gameParticipantRepo.findAllByGameAndStatus(game, ParticipantStatus.APPROVED);
        List<GameQuestion> gameQuestions = gameQuestionRepo.findAllByGame(game);

        for (int i = 0; i < gameParticipants.size(); i++) {
            for (int j = 0; j < gameQuestions.size(); j++) {
                QuestionResult questionResult = new QuestionResult();
                questionResult.setQuestion(gameQuestions.get(j));
                questionResult.setTeam(gameParticipants.get(i).getParticipant());
                questionResult.setCreatedAt(LocalDateTime.now());
                questionResultRepo.save(questionResult);
            }
            Result result = new Result();
            result.setGame(game);
            result.setTeam(gameParticipants.get(i).getParticipant());
            result.setPoints(0);
            result.setPlace(0);
            resultRepo.save(result);
        }


        return getGameQuestions(id, page, perPage, sort, order);
    }
}
