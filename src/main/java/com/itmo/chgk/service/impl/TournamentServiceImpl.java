package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Game;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.db.entity.TournamentTable;
import com.itmo.chgk.model.db.repository.GameRepo;
import com.itmo.chgk.model.db.repository.TournamentRepo;
import com.itmo.chgk.model.db.repository.TournamentTableRepo;
import com.itmo.chgk.model.dto.request.TournamentInfoRequest;
import com.itmo.chgk.model.dto.response.GameInfoResponse;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentTableInfoResponse;
import com.itmo.chgk.model.enums.GameStatus;
import com.itmo.chgk.model.enums.TournamentStatus;
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
    private final TournamentTableRepo tournamentTableRepo;

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
        return null;
    }

    @Override
    public Page<TeamInfoResponse> getTournamentParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<TournamentTableInfoResponse> getTournamentResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }
}
