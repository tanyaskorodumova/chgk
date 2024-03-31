package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.db.repository.*;
import com.itmo.chgk.model.dto.request.GameInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.model.enums.GameStatus;
import com.itmo.chgk.model.enums.Stage;
import com.itmo.chgk.model.enums.TournamentStatus;
import com.itmo.chgk.service.GameService;
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
    private final QuestionResultRepo questionResultRepo;

    private final TournamentService tournamentService;

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
        if (request.getDateTime() == null) {
            throw new CustomException("Необходимо ввести дату и время проведения игры", HttpStatus.BAD_REQUEST);
        }
        if (request.getPlace() == null) {
            throw new CustomException("Необходимо ввести место проведения игры", HttpStatus.BAD_REQUEST);
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
        return null;
    }

    @Override
    public void deleteGame(Long id) {

    }

    @Override
    public Page<TeamInfoResponse> addParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<TeamInfoResponse> approveParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<TeamInfoResponse> deleteParticipant(Long gameId, Long teamId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<TeamInfoResponse> getAllParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<TeamInfoResponse> getParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<QuestionInfoResponse> getGameQuestions(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<QuestionInfoResponse> setGameQuestions(Long id, QuestionPackRequest request, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<QuestionInfoResponse> setGameQuestion(Long gameId, Long questionId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<QuestionInfoResponse> deleteGameQuestion(Long gameId, Long questionId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
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
