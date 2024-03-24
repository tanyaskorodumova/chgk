package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    @GetMapping("/all")
    public Page<TeamInfoResponse> getAllTeams(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer perPage,
                                              @RequestParam(defaultValue = "points") String sort,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        //return questionService.getAllCars(page, perPage, sort, order);
        return null;
    }

    @GetMapping("/{id}")
    public TeamInfoResponse getTeam(@PathVariable Long id) {
        //return carService.getCar(id);
        return null;
    }

    @PostMapping("/new")
    public TeamInfoResponse createTeam(@RequestBody TeamInfoRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public TeamInfoResponse updateTeam(@PathVariable Long id, @RequestBody TeamInfoRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {

    }

    @PostMapping("/{teamId}/setMember/{userId}")
    public Page<UserInfoResponse> setMember(@PathVariable Long teamId, @PathVariable Long userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage,
                                            @RequestParam(defaultValue = "login") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @DeleteMapping("/{teamId}/deleteMember/{userId}")
    public Page<UserInfoResponse> deleteMember(@PathVariable Long teamId, @PathVariable Long userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer perPage,
                                            @RequestParam(defaultValue = "login") String sort,
                                            @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/{id}/members")
    public Page<UserInfoResponse> getMembers(@PathVariable Long id,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer perPage,
                                             @RequestParam(defaultValue = "login") String sort,
                                             @RequestParam(defaultValue = "ASC") Sort.Direction order) {
        return null;
    }

    @GetMapping("/rating")
    public Page<TeamInfoResponse> getRating(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer perPage,
                                              @RequestParam(defaultValue = "points") String sort,
                                              @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        //return questionService.getAllCars(page, perPage, sort, order);
        return null;
    }

}
