package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.request.TournamentInfoRequest;
import com.itmo.chgk.model.dto.response.GameInfoResponse;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentTableInfoResponse;
import com.itmo.chgk.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;

    @GetMapping("/all")
    public Page<TournamentInfoResponse> getAllTournaments(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer perPage,
                                                          @RequestParam(defaultValue = "id") String sort,
                                                          @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return tournamentService.getAllTournaments(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    public TournamentInfoResponse getTournament(@PathVariable Long id) {
        return tournamentService.getTournament(id);
    }

    @PostMapping("/new")
    public TournamentInfoResponse createTournament(@RequestBody @Valid TournamentInfoRequest request) {
        return tournamentService.createTournament(request);
    }

    @PutMapping("/{id}")
    public TournamentInfoResponse updateTournament(@PathVariable Long id, @RequestBody @Valid TournamentInfoRequest request) {
        return tournamentService.updateTournament(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
    }

    @GetMapping("/{id}/games")
    public Page<GameInfoResponse> getTournamentGames(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "stage") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return tournamentService.getTournamentGames(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants")
    public Page<TeamInfoResponse> getTournamentParticipants(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer perPage,
                                                            @RequestParam(defaultValue = "points") String sort,
                                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return tournamentService.getTournamentParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/results")
    public Page<TournamentTableInfoResponse> getTournamentsResults(@PathVariable Long id,
                                                                   @RequestParam(defaultValue = "1") Integer page,
                                                                   @RequestParam(defaultValue = "10") Integer perPage,
                                                                   @RequestParam(defaultValue = "points") String sort,
                                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return tournamentService.getTournamentResults(id, page, perPage, sort, order);
    }
}
