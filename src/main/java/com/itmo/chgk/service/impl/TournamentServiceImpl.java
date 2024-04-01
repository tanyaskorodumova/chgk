package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.*;
import com.itmo.chgk.model.db.repository.*;
import com.itmo.chgk.model.dto.request.TournamentInfoRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.model.enums.GameStatus;
import com.itmo.chgk.model.enums.ParticipantStatus;
import com.itmo.chgk.model.enums.TournamentStatus;
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
public class TournamentServiceImpl implements TournamentService {
    private final ObjectMapper mapper;

    private final TournamentRepo tournamentRepo;
    private final GameRepo gameRepo;
    private final GameParticipantRepo gameParticipantRepo;
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
        tournament.setCreatedAt(LocalDateTime.now());

        tournament = tournamentRepo.save(tournament);
        return mapper.convertValue(tournament, TournamentInfoResponse.class);
    }

    @Override
    public TournamentInfoResponse updateTournament(Long id, TournamentInfoRequest request) {
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
        tournamentTableRepo.findAllByTournament(tournament)
                .stream()
                .map(tournamentT -> {
                    tournamentT.setPoints(0);
                    tournamentT = tournamentTableRepo.save(tournamentT);
                    return tournamentT;
                });

        tournament.setUpdatedAt(LocalDateTime.now());

        tournament = tournamentRepo.save(tournament);
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
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Page<TournamentTableInfoResponse> getTournamentResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }
}
