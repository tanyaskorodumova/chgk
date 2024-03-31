package com.itmo.chgk.service;

import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.request.TournamentInfoRequest;
import com.itmo.chgk.model.dto.response.GameInfoResponse;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentTableInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface TournamentService {
    Page<TournamentInfoResponse> getAllTournaments(Integer page, Integer perPage, String sort, Sort.Direction order);

    TournamentInfoResponse getTournament(Long id);

    TournamentInfoResponse createTournament(TournamentInfoRequest request);

    TournamentInfoResponse updateTournament(Long id, TournamentInfoRequest request);

    void deleteTournament(Long id);

    Page<GameInfoResponse> getTournamentGames(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<TeamInfoResponse> getTournamentParticipants(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<TournamentTableInfoResponse> getTournamentResults(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Tournament getTournamentDb(Long id);
}
