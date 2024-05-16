package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.TournamentInfoRequest;
import com.itmo.chgk.model.dto.response.*;
import com.itmo.chgk.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;

    @GetMapping("/all")
    @Operation(summary = "Получение информации обо всех турнирах")
    public Page<TournamentInfoResponse> getAllTournaments(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer perPage,
                                                          @RequestParam(defaultValue = "id") String sort,
                                                          @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return tournamentService.getAllTournaments(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о конкретном турнире")
    public TournamentInfoResponse getTournament(@PathVariable Long id) {
        return tournamentService.getTournament(id);
    }

    @PostMapping("/new")
    @Operation(summary = "Создание турнира")
    public TournamentInfoResponse createTournament(@RequestBody @Valid TournamentInfoRequest request) {
        return tournamentService.createTournament(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактирование турнира")
    public TournamentInfoResponse updateTournament(@PathVariable Long id, @RequestBody @Valid TournamentInfoRequest request) {
        return tournamentService.updateTournament(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление турнира")
    public void deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
    }

    @GetMapping("/{id}/games")
    @Operation(summary = "Получение информации об играх турнира")
    public Page<GameInfoResponse> getTournamentGames(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "stage") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return tournamentService.getTournamentGames(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "Получение информации об участниках игр турнира")
    public Page<ParticipantsInfoResponse> getTournamentParticipants(@PathVariable Long id,
                                                                    @RequestParam(defaultValue = "1") Integer page,
                                                                    @RequestParam(defaultValue = "10") Integer perPage,
                                                                    @RequestParam(defaultValue = "id") String sort,
                                                                    @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return tournamentService.getTournamentParticipants(id, page, perPage, sort, order);
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Получение результатов завершенного турнира")
    public Page<TournamentTableInfoResponse> getTournamentsResults(@PathVariable Long id,
                                                                   @RequestParam(defaultValue = "1") Integer page,
                                                                   @RequestParam(defaultValue = "10") Integer perPage,
                                                                   @RequestParam(defaultValue = "points") String sort,
                                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return tournamentService.getTournamentResults(id, page, perPage, sort, order);
    }
}
