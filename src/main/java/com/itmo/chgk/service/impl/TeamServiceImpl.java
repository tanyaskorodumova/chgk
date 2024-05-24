package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Authority;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.entity.UserInfo;
import com.itmo.chgk.model.db.repository.TeamRepo;
import com.itmo.chgk.model.db.repository.UserInfoRepo;
import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.CommonStatus;
import com.itmo.chgk.model.enums.UserInfoRole;
import com.itmo.chgk.service.TeamService;
import com.itmo.chgk.service.UserInfoService;
import com.itmo.chgk.service.UserService;
import com.itmo.chgk.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {
    private final TeamRepo teamRepo;
    private final UserInfoRepo userInfoRepo;
    private final UserInfoService userInfoService;
    private final UserService userService;
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

        if (request.getTeamName() == null) {
            throw new CustomException("Необходимо указать название команды", HttpStatus.BAD_REQUEST);
        }

        // ПРОВЕРКА, ЧТО ТАКОЕ НАЗВ
        
        Team team = mapper.convertValue(request, Team.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        UserInfo captain = userInfoRepo.findByLogin(user.getUsername()).get();

        userInfoService.setRole(captain.getId(), UserInfoRole.CAPTAIN);
        userService.addAuthority(captain.getLogin(), List.of("ROLE_CAPTAIN"));
        team.setTeamName(request.getTeamName());
        team.setCaptain(captain);

        long viceId = request.getViceCaptainId();
        if (viceId != 0 && viceId != captain.getId()) {
            UserInfo viceCaptain = userInfoService.getUserDb(viceId);
            userInfoService.setRole(viceId, UserInfoRole.VICECAPTAIN);
            UserInfo vice = userInfoService.getUserDb(viceId);
            userService.addAuthority(vice.getLogin(), List.of("ROLE_VICECAPTAIN"));
            team.setViceCaptain(viceCaptain);
        }

        team.setStatus(CommonStatus.CREATED);
        team.setCreatedAt(LocalDateTime.now());

        team = teamRepo.save(team);

        return getTeam(team.getId());
    }

    @Override
    public TeamInfoResponse updateTeam(Long id, TeamInfoRequest request) {
        Team team = getTeamDb(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userName = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        UserInfo captain = team.getCaptain();
        String captainName = captain.getLogin();

        // установка ограничения - разрешено вносить изменения только в своей команде (админ может в любой)
        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (team.getViceCaptain() == null) {
                if (!userName.equals(captainName)) {
                    throw new CustomException("Пользователь не имеет прав на редактирование данной команды", HttpStatus.FORBIDDEN);
                }
            } else {
                String viceName = team.getViceCaptain().getLogin();
                if (!userName.equals(captainName) && !userName.equals(viceName)) {
                    throw new CustomException("Пользователь не имеет прав на редактирование данной команды", HttpStatus.FORBIDDEN);
                }
            }
        }

        // смена названия команды
        team.setTeamName(request.getTeamName() == null ? team.getTeamName() : request.getTeamName());

        // замена капитана
        if (request.getCaptainId() != 0 && request.getCaptainId()!=captain.getId()) {
            if (team.getViceCaptain() != null) {
                String viceName = team.getViceCaptain().getLogin();
                if (userName.equals(viceName)) {
                    throw new CustomException("Вице-капитан не может менять капитана", HttpStatus.FORBIDDEN);
                }
            }

            UserInfo newCaptainUI = userInfoService.getUserDb(request.getCaptainId());
            User newCaptain = userService.getUser(newCaptainUI.getLogin());
            Collection<? extends GrantedAuthority> newCaptainAuthorities = newCaptain.getAuthorities();
            List<String> newCaptainListAuthorities = newCaptainAuthorities
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            if (newCaptainListAuthorities.contains("ROLE_CAPTAIN")) {
                throw new CustomException("Пользователь уже является капитаном другой команды", HttpStatus.BAD_REQUEST);
            }

            if(team.getViceCaptain() != null && request.getCaptainId()==team.getViceCaptain().getId()){
                userService.deleteAuthority(team.getViceCaptain().getLogin(), "ROLE_VICECAPTAIN");
                team.setViceCaptain(null);
            }

            userService.deleteAuthority(team.getCaptain().getLogin(), "ROLE_CAPTAIN");
            userService.addAuthority(team.getCaptain().getLogin(), List.of("ROLE_USER"));
            userInfoService.setRole(team.getCaptain().getId(), UserInfoRole.USER);

            userInfoService.setRole(newCaptainUI.getId(), UserInfoRole.CAPTAIN);
            userService.addAuthority(newCaptain.getUsername(), List.of("ROLE_CAPTAIN"));
            team.setCaptain(newCaptainUI);
        }

        // замена вице-капитана
        if (request.getViceCaptainId() == 0) {
            if (team.getViceCaptain() != null) {
                UserInfo viceCaptainUI = team.getViceCaptain();
                userInfoService.setRole(viceCaptainUI.getId(), UserInfoRole.USER);
                userService.deleteAuthority(viceCaptainUI.getLogin(), "ROLE_VICECAPTAIN");
                userService.addAuthority(viceCaptainUI.getLogin(), List.of("ROLE_USER"));
                team.setViceCaptain(null);
            } else {}
        } else {
            UserInfo newViceCaptainUI = userInfoService.getUserDb(request.getViceCaptainId());
            User newViceCaptain = userService.getUser(newViceCaptainUI.getLogin());
            Collection<? extends GrantedAuthority> newViceCaptainAuthorities = newViceCaptain.getAuthorities();
            List<String> newViceCaptainListAuthorities = newViceCaptainAuthorities
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            if (newViceCaptainListAuthorities.contains("ROLE_VICECAPTAIN")) {
                if (team.getViceCaptain() == null || team.getViceCaptain().getId() != request.getViceCaptainId())
                    throw new CustomException("Пользователь уже является вице-капитаном другой команды", HttpStatus.BAD_REQUEST);
            }

            if (request.getViceCaptainId() == team.getCaptain().getId()) {
                throw new CustomException("Пользователь уже является капитаном этой команды", HttpStatus.BAD_REQUEST);
            } else {
                if (team.getViceCaptain() == null) {
                    userInfoService.setRole(newViceCaptainUI.getId(), UserInfoRole.VICECAPTAIN);
                    userService.addAuthority(newViceCaptainUI.getLogin(), List.of("ROLE_VICECAPTAIN"));
                    team.setViceCaptain(newViceCaptainUI);
                } else {
                    if (team.getViceCaptain().getId() != request.getViceCaptainId()) {
                        UserInfo viceCaptainUI = team.getViceCaptain();
                        userInfoService.setRole(viceCaptainUI.getId(), UserInfoRole.USER);
                        userService.deleteAuthority(viceCaptainUI.getLogin(), "ROLE_VICECAPTAIN");
                        userService.addAuthority(viceCaptainUI.getLogin(), List.of("ROLE_USER"));

                        userInfoService.setRole(newViceCaptainUI.getId(), UserInfoRole.VICECAPTAIN);
                        userService.addAuthority(newViceCaptainUI.getLogin(), List.of("ROLE_VICECAPTAIN"));
                        team.setViceCaptain(newViceCaptainUI);
                    } else {}
                }
            }
        }

        team.setStatus(CommonStatus.UPDATED);
        team.setUpdatedAt(LocalDateTime.now());

        team = teamRepo.save(team);

        return getTeam(team.getId());
    }

    @Override
    public void deleteTeam(Long id) {
        Team team = getTeamDb(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userName = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfo captain = team.getCaptain();
        String captainName = captain.getLogin();

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (team.getViceCaptain() == null) {
                if (!userName.equals(captainName)) {
                    throw new CustomException("Пользователь не имеет прав на удаление данной команды", HttpStatus.FORBIDDEN);
                }
            } else {
                String viceName = team.getViceCaptain().getLogin();
                if (!userName.equals(captainName) && !userName.equals(viceName)) {
                    throw new CustomException("Пользователь не имеет прав на удаление данной команды", HttpStatus.FORBIDDEN);
                }
            }
        }

        userService.deleteAuthority(captainName, "ROLE_CAPTAIN");
        userService.addAuthority(captainName, List.of("ROLE_USER"));

        if (team.getViceCaptain()!=null) {
            String viceName = team.getViceCaptain().getLogin();
            userService.deleteAuthority(viceName, "ROLE_VICECAPTAIN");
            userService.addAuthority(viceName, List.of("ROLE_USER"));
        }

        team.setCaptain(null);
        team.setViceCaptain(null);
        team.setUpdatedAt(LocalDateTime.now());
        team.setStatus(CommonStatus.DELETED);
        teamRepo.save(team);
    }

    @Override
    public Page<UserInfoResponse> setMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Team team = getTeamDb(teamId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userName = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfo captain = team.getCaptain();
        String captainName = captain.getLogin();

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (team.getViceCaptain() == null) {
                if (!userName.equals(captainName)) {
                    throw new CustomException("Пользователь не имеет прав на изменение состава данной команды", HttpStatus.FORBIDDEN);
                }
            } else {
                String viceName = team.getViceCaptain().getLogin();
                if (!userName.equals(captainName) && !userName.equals(viceName)) {
                    throw new CustomException("Пользователь не имеет прав на изменение состава данной команды", HttpStatus.FORBIDDEN);
                }
            }
        }

        UserInfo userInfo = userInfoService.getUserDb(userId);

        List<UserInfo> members = team.getUserInfos();
        members.add(userInfo);
        userInfo.setTeam(team);
        team = teamRepo.save(team);
        userInfoRepo.save(userInfo);

        return getMembers(teamId, page, perPage, sort, order);
    }

    @Override
    public Page<UserInfoResponse> deleteMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Team team = getTeamDb(teamId);
        List<UserInfo> members = team.getUserInfos();
        UserInfo userInfo = userInfoService.getUserDb(userId);

        if (!members.contains(userInfo)){
            throw new CustomException("Указанный пользователь не является членом этой команды", HttpStatus.FORBIDDEN);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfo captain = team.getCaptain();
        String captainName = captain.getLogin();


        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (!userInfo.getLogin().equals(userName)) {
                if (team.getViceCaptain() == null) {
                    if (!userName.equals(captainName)) {
                        throw new CustomException("Пользователь не имеет прав на изменение состава данной команды", HttpStatus.FORBIDDEN);
                    }
                } else {
                    String viceName = team.getViceCaptain().getLogin();
                    if (!userName.equals(captainName) && !userName.equals(viceName)) {
                        throw new CustomException("Пользователь не имеет прав на изменение состава данной команды", HttpStatus.FORBIDDEN);
                    }
                }
            }
        }

        userInfo.setTeam(null);
        userInfoRepo.save(userInfo);


        members.remove(userInfo);
        teamRepo.save(team);

        return getMembers(teamId, page, perPage, sort, order);
    }

    @Override
    public Page<UserInfoResponse> getMembers(Long id, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);
        Team team = getTeamDb(id);

        List<UserInfoResponse> allUsers = userInfoRepo.findAllByTeam(team.getId(), request)
                .getContent()
                .stream()
                .map(u -> userInfoService.getUser(u.getId()))
                .collect(Collectors.toList());

        return new PageImpl<>(allUsers);
    }

}
