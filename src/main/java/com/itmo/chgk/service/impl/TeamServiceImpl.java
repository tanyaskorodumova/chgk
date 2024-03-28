package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.repository.TeamRepo;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.CommonStatus;
import com.itmo.chgk.model.enums.QuestionStatus;
import com.itmo.chgk.service.TeamService;
import com.itmo.chgk.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {
    private final TeamRepo teamRepo;
    private final UserRepo userRepo;

    private final ObjectMapper mapper;

    @Override
    public Page<TeamInfoResponse> getAllTeams(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<TeamInfoResponse> allTeams = teamRepo.findAllByStatusIsNot(request, CommonStatus.DELETED)
                .getContent()
                .stream()
                .map(team -> mapper.convertValue(team, TeamInfoResponse.class))
                .collect(Collectors.toList());

        return new PageImpl<>(allTeams);
    }

    public Team getTeamDb(Long id) {
        return teamRepo.findById(id).orElseThrow(() -> new CustomException("Команда не найдена", HttpStatus.NOT_FOUND));
    }

    @Override
    public TeamInfoResponse getTeam(Long id) {
        Team team = getTeamDb(id);
        TeamInfoResponse response = mapper.convertValue(team, TeamInfoResponse.class);
        return response;
    }

    @Override
    public TeamInfoResponse createTeam(TeamInfoRequest request) {
        return null;
    }

    @Override
    public TeamInfoResponse updateTeam(Long id, TeamInfoRequest request) {
        return null;
    }

    @Override
    public void deleteTeam(Long id) {

    }

    @Override
    public Page<UserInfoResponse> setMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<UserInfoResponse> deleteMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<UserInfoResponse> getMembers(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }

    @Override
    public Page<TeamInfoResponse> getRating(Integer page, Integer perPage, String sort, Sort.Direction order) {
        return null;
    }
}
