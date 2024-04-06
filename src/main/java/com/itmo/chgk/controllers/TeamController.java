package com.itmo.chgk.controllers;

import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping("/all")
    public Page<TeamInfoResponse> getAllTeams(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer perPage,
                                              @RequestParam(defaultValue = "points") String sort,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return teamService.getAllTeams(page, perPage, sort, order);
    }

    @GetMapping("/{id}")
    public TeamInfoResponse getTeam(@PathVariable Long id) {
        return teamService.getTeam(id);
    }

    @PostMapping("/new")
    public TeamInfoResponse createTeam(@RequestBody TeamInfoRequest request) {
        return teamService.createTeam(request);
//
//        for (int i = 1; i <= 500; i++) {
//            request.setTeamName("Team" + i);
//            request.setCaptainId((long) (Math.random() * 4000 + 1));
//            request.setViceCaptainId(Math.random() > 0.25 ? (long) (Math.random() * 3000 + 4001) : null);
//            teamService.createTeam(request);
//        }
//        return null;
    }

    @PutMapping("/{id}")
    public TeamInfoResponse updateTeam(@PathVariable Long id, @RequestBody @Valid TeamInfoRequest request) {
        return teamService.updateTeam(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
    }

    @PostMapping("/{teamId}/setMember/{userId}")
    public Page<UserInfoResponse> setMember(@PathVariable Long teamId, @PathVariable Long userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage)//,
                                            //@RequestParam(defaultValue = "login") String sort,
                                            //@RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        return teamService.setMember(teamId, userId, page, perPage, null, null);

//        userId = 2L;
//        for (int i = 1; i < 500; i++) {
//            teamId = (long) i;
//            int maxMembs = (int) (Math.random() * 13 + 1);
//            for (int j = 0; j < maxMembs; j++) {
//                teamService.setMember(teamId, ++userId, page, perPage, sort, order);
//                //userId++;
//            }
//        }
//
//        return null;
    }

    @DeleteMapping("/{teamId}/deleteMember/{userId}")
    public Page<UserInfoResponse> deleteMember(@PathVariable Long teamId, @PathVariable Long userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage,
                                            @RequestParam(defaultValue = "login") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return teamService.deleteMember(teamId, userId, page, perPage, sort, order);
    }

    @GetMapping("/{id}/members")
    public Page<UserInfoResponse> getMembers(@PathVariable Long id,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer perPage,
                                             @RequestParam(defaultValue = "id") String sort,
                                             @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return teamService.getMembers(id, page, perPage, sort, order);
    }

}
