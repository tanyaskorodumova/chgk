package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.repository.TeamRepo;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.CommonStatus;
import com.itmo.chgk.model.enums.UserRole;
import com.itmo.chgk.service.LoggedUserManagementService;
import com.itmo.chgk.service.TeamService;
import com.itmo.chgk.service.UserService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {
    private final TeamRepo teamRepo;
    private final UserRepo userRepo;
    private final UserService userService;
    private final LoggedUserManagementService loggedUserManagementService;

    private final ObjectMapper mapper;

    @Override
    public Page<TeamInfoResponse> getAllTeams(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<TeamInfoResponse> allTeams = teamRepo.findAllByStatusIsNot(request, CommonStatus.DELETED)
                .getContent()
                .stream()
                .map(team -> {
                    TeamInfoResponse response = mapper.convertValue(team, TeamInfoResponse.class);
                    UserInfoResponse captain = mapper.convertValue(team.getCaptain(), UserInfoResponse.class);
                    captain.setPassword("Скрыто");
                    UserInfoResponse viceCaptain = mapper.convertValue(team.getViceCaptain(), UserInfoResponse.class);
                    if (viceCaptain != null) {
                        viceCaptain.setPassword("Скрыто");
                    }
                    response.setCaptain(captain);
                    response.setViceCaptain(viceCaptain);
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(allTeams);
    }

    @Override
    public Team getTeamDb(Long id) {
        return teamRepo.findById(id).orElseThrow(() -> new CustomException("Команда не найдена", HttpStatus.NOT_FOUND));
    }

    @Override
    public TeamInfoResponse getTeam(Long id) {
        Team team = getTeamDb(id);
        TeamInfoResponse response = mapper.convertValue(team, TeamInfoResponse.class);
        UserInfoResponse captain = mapper.convertValue(team.getCaptain(), UserInfoResponse.class);
        captain.setPassword("Скрыто");
        UserInfoResponse viceCaptain = team.getViceCaptain() == null ? null : mapper.convertValue(team.getViceCaptain(), UserInfoResponse.class);
        if (viceCaptain != null) {
            viceCaptain.setPassword("Скрыто");
        }
        response.setCaptain(captain);
        response.setViceCaptain(viceCaptain);
        return response;
    }

    @Override
    public TeamInfoResponse createTeam(TeamInfoRequest request) {
        if (loggedUserManagementService.getUser() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.LOCKED);
        }

        if (request.getTeamName() == null) {
            throw new CustomException("Необходимо указать название команды", HttpStatus.BAD_REQUEST);
        }

        if (request.getCaptainId() == null) {
            throw new CustomException("Необходимо указать id капитана", HttpStatus.BAD_REQUEST);
        } else if (!request.getCaptainId().equals(loggedUserManagementService.getUser().getId())) {
            throw new CustomException("Капитаном должен быть создатель команды", HttpStatus.LOCKED);
        }

        Team team = mapper.convertValue(request, Team.class);

        User captain = userService.getUserDb(request.getCaptainId());
        userService.setRole(captain.getId(), UserRole.CAPTAIN);
        loggedUserManagementService.setUser(captain);
        team.setTeamName(request.getTeamName());
        team.setCaptain(captain);

        if (request.getViceCaptainId() != null) {
            User viceCaptain = userService.getUserDb(request.getViceCaptainId());
            userService.setRole(viceCaptain.getId(), UserRole.VICECAPTAIN);
            team.setViceCaptain(viceCaptain);
        }

        team.setStatus(CommonStatus.CREATED);
        team.setCreatedAt(LocalDateTime.now());

        team = teamRepo.save(team);
        loggedUserManagementService.setTeamId(team.getId());

        return getTeam(team.getId());
    }

    @Override
    public TeamInfoResponse updateTeam(Long id, TeamInfoRequest request) {
        if (loggedUserManagementService.getUser() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.LOCKED);
        } else if (!loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) &&
                    !loggedUserManagementService.getUser().getRole().equals(UserRole.VICECAPTAIN) &&
                    !loggedUserManagementService.getUser().getRole().equals(UserRole.ADMIN)) {
            throw new CustomException("Пользователь не имеет прав на редактирование команды", HttpStatus.LOCKED);
        } else if ((loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) ||
                loggedUserManagementService.getUser().getRole().equals(UserRole.VICECAPTAIN)) &&
                !loggedUserManagementService.getTeamId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на редактирование данной команды", HttpStatus.LOCKED);
        }

        Team team = getTeamDb(id);
        team.setTeamName(request.getTeamName() == null ? team.getTeamName() : request.getTeamName());

        if (request.getCaptainId() != null) {
            if (!loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) &&
                !loggedUserManagementService.getUser().getRole().equals(UserRole.ADMIN)) {
                throw new CustomException("Необходимы права капитана или администратора", HttpStatus.LOCKED);
            }
            User captain = userService.getUserDb(request.getCaptainId());
            userService.setRole(captain.getId(), UserRole.CAPTAIN);
            team.setCaptain(captain);
        }

        if (request.getViceCaptainId() != null) {
            User viceCaptain = userService.getUserDb(request.getViceCaptainId());
            userService.setRole(viceCaptain.getId(), UserRole.VICECAPTAIN);
            team.setViceCaptain(viceCaptain);
        }

        team.setStatus(CommonStatus.UPDATED);
        team.setUpdatedAt(LocalDateTime.now());

        team = teamRepo.save(team);

        return getTeam(team.getId());
    }

    @Override
    public void deleteTeam(Long id) {
        if (loggedUserManagementService.getUser() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.LOCKED);
        } else if (!loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) &&
                !loggedUserManagementService.getUser().getRole().equals(UserRole.VICECAPTAIN) &&
                !loggedUserManagementService.getUser().getRole().equals(UserRole.ADMIN)) {
            throw new CustomException("Пользователь не имеет прав на удаление команды", HttpStatus.LOCKED);
        } else if ((loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) ||
                loggedUserManagementService.getUser().getRole().equals(UserRole.VICECAPTAIN)) &&
                !loggedUserManagementService.getTeamId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на удаление данной команды", HttpStatus.LOCKED);
        }

        Team team = getTeamDb(id);

        team.setUpdatedAt(LocalDateTime.now());
        team.setStatus(CommonStatus.DELETED);
        teamRepo.save(team);
    }

    @Override
    public Page<UserInfoResponse> setMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        if (loggedUserManagementService.getUser() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.LOCKED);
        } else if (!loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) &&
                !loggedUserManagementService.getUser().getRole().equals(UserRole.VICECAPTAIN) &&
                !loggedUserManagementService.getUser().getRole().equals(UserRole.ADMIN)) {
            throw new CustomException("Пользователь не имеет прав на редактирование состава команды", HttpStatus.LOCKED);
        } else if ((loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) ||
                loggedUserManagementService.getUser().getRole().equals(UserRole.VICECAPTAIN)) &&
                !loggedUserManagementService.getTeamId().equals(teamId)) {
            throw new CustomException("Пользователь не имеет прав на редактирование состава данной команды", HttpStatus.LOCKED);
        }

        Team team = getTeamDb(teamId);
        User user = userService.getUserDb(userId);

        List<User> members = team.getUsers();
        members.add(user);
        user.setTeam(team);
        team = teamRepo.save(team);
        userRepo.save(user);

        return getMembers(teamId, page, perPage, sort, order);
    }

    @Override
    public Page<UserInfoResponse> deleteMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        if (loggedUserManagementService.getUser() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.LOCKED);
        } else if (!loggedUserManagementService.getUser().getRole().equals(UserRole.ADMIN) &&
                !loggedUserManagementService.getUser().getRole().equals(UserRole.CAPTAIN) &&
                !loggedUserManagementService.getUser().getRole().equals(UserRole.VICECAPTAIN) &&
                !loggedUserManagementService.getUser().getId().equals(userId)) {
            throw new CustomException("Пользователь не имеет прав на удаление данного пользователя из команды", HttpStatus.LOCKED);
        }

        Team team = getTeamDb(teamId);
        User user = userService.getUserDb(userId);

        List<User> members = team.getUsers();
        members.remove(user);
        teamRepo.save(team);

        return getMembers(teamId, page, perPage, sort, order);
    }

    @Override
    public Page<UserInfoResponse> getMembers(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);
        Team team = getTeamDb(id);

        List<UserInfoResponse> allUsers = userRepo.findAllByTeam(team.getId(), request)
                .getContent()
                .stream()
                .map(u -> userService.getUser(u.getId()))
                .collect(Collectors.toList());

        return new PageImpl<>(allUsers);
    }

}
