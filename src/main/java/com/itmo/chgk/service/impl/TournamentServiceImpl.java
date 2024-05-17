package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.*;
import com.itmo.chgk.model.db.repository.*;
import com.itmo.chgk.model.dto.request.TournamentInfoRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.model.enums.*;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TournamentServiceImpl implements TournamentService {
    private final ObjectMapper mapper;

    private final TournamentRepo tournamentRepo;
    private final GameRepo gameRepo;
    private final GameParticipantRepo gameParticipantRepo;
    private final ResultRepo resultRepo;
    private final TeamRepo teamRepo;
    private final TournamentTableRepo tournamentTableRepo;
    private final TeamService teamService;

    @Override
    public Page<TournamentInfoResponse> getAllTournaments(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<TournamentInfoResponse> all = tournamentRepo.findAll(request)
                .getContent()
                .stream()
                .map(tournament -> mapper.convertValue(tournament, TournamentInfoResponse.class))
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Tournament getTournamentDb(Long id) {
        return tournamentRepo.findById(id).orElseThrow(() -> new CustomException("Турнир не найден", HttpStatus.NOT_FOUND));
    }

    @Override
    public TournamentInfoResponse getTournament(Long id) {
        Tournament tournament = getTournamentDb(id);
        return mapper.convertValue(tournament, TournamentInfoResponse.class);
    }

    @Override
    public TournamentInfoResponse createTournament(TournamentInfoRequest request) {
        if (false) {  // требуется проверка, что пользователь авторизован
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (false) {  // требуется проверка, что пользователь ADMIN или ORGINIZER
            throw new CustomException("У пользователя нет прав на создание турнира", HttpStatus.FORBIDDEN);
        }

        if (request.getTournName() == null) {
            throw new CustomException("Необходимо указать название турнира", HttpStatus.BAD_REQUEST);
        }
        if (request.getLevel() == null) {
            throw new CustomException("Необходимо указать уровень турнира", HttpStatus.BAD_REQUEST);
        }
        if (request.getTournFactor() == null) {
            throw new CustomException("Необходимо указать значимость турнира", HttpStatus.BAD_REQUEST);
        }

        Tournament tournament = mapper.convertValue(request, Tournament.class);

        tournament.setMinPoints(tournament.getMinPoints() == null ? 0 : tournament.getMinPoints());
        tournament.setStatus(TournamentStatus.PLANNED);
    //    tournament.setOrganizer(loggedUserManagementService.getUserInfo());  // требуется получение информации об имени этого пользователя(авторизованного)
        tournament.setCreatedAt(LocalDateTime.now());

        tournament = tournamentRepo.save(tournament);
        return mapper.convertValue(tournament, TournamentInfoResponse.class);
    }

    @Override
    public TournamentInfoResponse updateTournament(Long id, TournamentInfoRequest request) {
        if (false) {  // Требуется проверка авторизации
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (false) {  // Требуется проверка, что пользователь ADMIN или ORGANIZER
            throw new CustomException("У пользователя нет прав на редактирование турнира", HttpStatus.FORBIDDEN);
        } else if (false) {  // Требуется проверка, что пользователь ORGANIZER этого турнира
            throw new CustomException("У пользователя нет прав на редактирование данного турнира", HttpStatus.FORBIDDEN);
        }

        Tournament tournament = getTournamentDb(id);

        if (tournament.getStatus().equals(TournamentStatus.FINAL) ||
            tournament.getStatus().equals(TournamentStatus.SEMIFINALS) ||
            tournament.getStatus().equals(TournamentStatus.QUARTERFINALS) ||
            tournament.getStatus().equals(TournamentStatus.QUALIFYING) ||
            tournament.getStatus().equals(TournamentStatus.FINISHED)) {
            throw new CustomException("Турнир начался: внести изменения нельзя", HttpStatus.BAD_REQUEST);
        }

        tournament.setTournName(request.getTournName() == null ? tournament.getTournName() : request.getTournName());
        tournament.setLevel(request.getLevel() == null ? tournament.getLevel() : request.getLevel());
        tournament.setTournFactor(request.getTournFactor() == null ? tournament.getTournFactor() : request.getTournFactor());
        tournament.setMinPoints(request.getMinPoints() == null ? tournament.getMinPoints() : request.getMinPoints());

        tournament.setUpdatedAt(LocalDateTime.now());
        tournament = tournamentRepo.save(tournament);

        return mapper.convertValue(tournament, TournamentInfoResponse.class);
    }

    @Override
    public void deleteTournament(Long id) {
        if (false) {  // Требуется проверка авторизации
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (false) {  // Требуется проверка что пользователь ADMIN или ORGANIZER
            throw new CustomException("У пользователя нет прав на удаление турнира", HttpStatus.FORBIDDEN);
        } else if (false) {  // Требуется проверка, что пользователь ORGANIZER этого удаляемого турнира
            throw new CustomException("У пользователя нет прав на удаление данного турнира", HttpStatus.FORBIDDEN);
        }

        Tournament tournament = getTournamentDb(id);
        if (tournament.getStatus().equals(TournamentStatus.FINISHED)) {
            throw new CustomException("Нельзя отменить завершенный турнир", HttpStatus.BAD_REQUEST);
        }
        tournament.setStatus(TournamentStatus.CANCELLED);
        tournament.getGames()
                .stream()
                .map(game -> {
                    game.setStatus((game.getStatus().equals(GameStatus.PLANNED)) ? GameStatus.CANCELLED : game.getStatus());
                    game = gameRepo.save(game);
                    return game;
                });

        tournament.setUpdatedAt(LocalDateTime.now());

        tournamentRepo.save(tournament);
    }

    @Override
    public Page<GameInfoResponse> getTournamentGames(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        Tournament tournament = getTournamentDb(id);

        List<GameInfoResponse> all = gameRepo.findAllByTournamentAndStatusIsNot(tournament, GameStatus.CANCELLED, request)
                .getContent()
                .stream()
                .map(game -> {
                    GameInfoResponse response = mapper.convertValue(game, GameInfoResponse.class);
                    response.setTournament(mapper.convertValue(tournament, TournamentInfoResponse.class));
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<ParticipantsInfoResponse> getTournamentParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        Tournament tournament = getTournamentDb(id);

        List<ParticipantsInfoResponse> all = gameParticipantRepo.findAllByTournament(tournament, request)
                .getContent()
                .stream()
                .map(gameParticipant -> {
                    TeamInfoResponse teamInfoResponse = teamService.getTeam(gameParticipant.getParticipant().getId());
                    ParticipantStatus status = gameParticipant.getStatus();
                    ParticipantsInfoResponse response = mapper.convertValue(teamInfoResponse, ParticipantsInfoResponse.class);
                    response.setParticipantStatus(status);
                    response.setGameId(gameParticipant.getGame().getId());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<TournamentTableInfoResponse> getTournamentResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Tournament tournament = getTournamentDb(id);

        if (!tournament.getStatus().equals(TournamentStatus.FINISHED)) {
            throw new CustomException("Турнир еще не завершен", HttpStatus.BAD_REQUEST);
        }

        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<TournamentTableInfoResponse> all = tournamentTableRepo.findAllByTournament(tournament, request)
                .getContent()
                .stream()
                .map(tournamentTable -> {
                    TournamentTableInfoResponse response = new TournamentTableInfoResponse();
                    response.setTournament(mapper.convertValue(tournamentTable.getTournament(), TournamentInfoResponse.class));
                    response.setTeamId(tournamentTable.getTeam().getId());
                    response.setTeamName(tournamentTable.getTeam().getTeamName());
                    response.setPoints(tournamentTable.getPoints());
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public void countResults(Long id) {
        Tournament tournament = getTournamentDb(id);

        List<TournamentTable> finalists = resultRepo.findAllByTournamentAndStageAndPlaceAfter(tournament, Stage.FINAL, 10)
                .stream()
                .map(result -> {
                    TournamentTable tournamentTable = new TournamentTable();
                    tournamentTable.setTournament(tournament);
                    Integer points = result.getPlace() == 1 ? 100 : 100 - 5 * result.getPlace();
                    tournamentTable.setPoints(points);
                    Team team = result.getTeam();
                    team.setPoints(team.getPoints() == null ? points * tournament.getTournFactor()
                            : team.getPoints() + points * tournament.getTournFactor());
                    team = teamRepo.save(team);
                    tournamentTable.setTeam(team);
                    return tournamentTableRepo.save(tournamentTable);
                })
                .collect(Collectors.toList());

        AtomicReference<Integer> semiPoints = new AtomicReference<>(44);
        List<TournamentTable> semiFinalists = resultRepo.findAllByTournamentAndStageAndPlaceAfter(tournament, Stage.SEMIFINAL, 6)
                .stream()
                .map(result -> {
                    TournamentTable tournamentTable = new TournamentTable();
                    tournamentTable.setTournament(tournament);
                    Integer points = semiPoints.get();
                    tournamentTable.setPoints(points);
                    Team team = result.getTeam();
                    team.setPoints(team.getPoints() == null ? points * tournament.getTournFactor()
                            : team.getPoints() + points * tournament.getTournFactor());
                    tournamentTable.setTeam(teamRepo.save(team));
                    semiPoints.updateAndGet(v -> v - 2);
                    return  tournamentTableRepo.save(tournamentTable);
                })
                .collect(Collectors.toList());

        AtomicReference<Integer> quarterPoints = new AtomicReference<>(20);
        List<TournamentTable> quarterFinalists = resultRepo.findAllByTournamentAndStageAndPlaceAfter(tournament, Stage.QUARTERFINAL, 6)
                .stream()
                .map(result -> {
                    TournamentTable tournamentTable = new TournamentTable();
                    tournamentTable.setTournament(tournament);
                    Integer points = quarterPoints.get();
                    tournamentTable.setPoints(points);
                    Team team = result.getTeam();
                    team.setPoints(team.getPoints() == null ? points * tournament.getTournFactor()
                            : team.getPoints() + points * tournament.getTournFactor());
                    tournamentTable.setTeam(teamRepo.save(team));
                    quarterPoints.updateAndGet(v -> v - 1);
                    return  tournamentTableRepo.save(tournamentTable);
                })
                .collect(Collectors.toList());

        tournament.setStatus(TournamentStatus.FINISHED);
        tournamentRepo.save(tournament);

    }
}
