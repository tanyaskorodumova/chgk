package com.itmo.chgk.service;

import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface TeamService {
    Page<TeamInfoResponse> getAllTeams(Integer page, Integer perPage, String sort, Sort.Direction order);

    TeamInfoResponse getTeam(Long id);

    TeamInfoResponse createTeam(TeamInfoRequest request);

    TeamInfoResponse updateTeam(Long id, TeamInfoRequest request);

    void deleteTeam(Long id);

    Page<UserInfoResponse> setMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<UserInfoResponse> deleteMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<UserInfoResponse> getMembers(Long id, Integer page, Integer perPage, String sort, Sort.Direction order);

    Page<TeamInfoResponse> getRating(Integer page, Integer perPage, String sort, Sort.Direction order);
}
