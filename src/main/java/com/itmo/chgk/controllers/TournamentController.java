package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.request.TournamentInfoRequest;
import com.itmo.chgk.model.dto.response.GameInfoResponse;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentInfoResponse;
import com.itmo.chgk.model.dto.response.TournamentTableInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    @GetMapping("/all")
    public Page<TournamentInfoResponse> getAllTournaments(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer perPage,
                                                          @RequestParam(defaultValue = "id") String sort,
                                                          @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        //return questionService.getAllCars(page, perPage, sort, order);
        return null;
    }

    @GetMapping("/{id}")
    public TournamentInfoResponse getTournament(@PathVariable Long id) {
        //return carService.getCar(id);
        return null;
    }

    @PostMapping("/new")
    public TournamentInfoResponse createTournament(@RequestBody TeamInfoRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public TournamentInfoResponse updateTournament(@PathVariable Long id, @RequestBody TournamentInfoRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteTournament(@PathVariable Long id) {

    }

    @GetMapping("/{id}/games")
    public Page<GameInfoResponse> getTournamentGames(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer perPage,
                                                     @RequestParam(defaultValue = "stage") String sort,
                                                     @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/participants")
    public Page<TeamInfoResponse> getTournamentParticipants(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer perPage,
                                                            @RequestParam(defaultValue = "points") String sort,
                                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/results")
    public Page<TournamentTableInfoResponse> getTournamentsResults(@PathVariable Long id,
                                                                   @RequestParam(defaultValue = "1") Integer page,
                                                                   @RequestParam(defaultValue = "10") Integer perPage,
                                                                   @RequestParam(defaultValue = "points") String sort,
                                                                   @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }
}
