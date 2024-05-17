package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping("/all")
    @Operation(summary = "Получение информации обо всех командах")
    public Page<TeamInfoResponse> getAllTeams(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer perPage,
                                              @RequestParam(defaultValue = "points") String sort,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return teamService.getAllTeams(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о конкретной команде")
    public TeamInfoResponse getTeam(@PathVariable Long id) {
        return teamService.getTeam(id);
    }

    @PostMapping("/new")
    @Operation(summary = "Создание команды")
    public TeamInfoResponse createTeam(@RequestBody TeamInfoRequest request) {
        return teamService.createTeam(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Редактирование информации о команде")
    public TeamInfoResponse updateTeam(@PathVariable Long id, @RequestBody @Valid TeamInfoRequest request) {
        return teamService.updateTeam(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление команды")
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
    }

    @PostMapping("/{teamId}/setMember/{userInfoId}")
    @Operation(summary = "Добавление участника команды")
    public Page<UserInfoResponse> setMember(@PathVariable Long teamId, @PathVariable Long userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage)
    {
        return teamService.setMember(teamId, userId, page, perPage, null, null);
    }

    @DeleteMapping("/{teamId}/deleteMember/{userInfoId}")
    @Operation(summary = "Удаление участника из команды")
    public Page<UserInfoResponse> deleteMember(@PathVariable Long teamId, @PathVariable Long userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage,
                                            @RequestParam(defaultValue = "login") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return teamService.deleteMember(teamId, userId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Получение информации об участниках команды")
    public Page<UserInfoResponse> getMembers(@PathVariable Long id,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer perPage,
                                             @RequestParam(defaultValue = "id") String sort,
                                             @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return teamService.getMembers(id, page, perPage, sort, order);
    }

}
