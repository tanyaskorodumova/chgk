package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.entity.UserInfo;
import com.itmo.chgk.model.db.repository.TeamRepo;
import com.itmo.chgk.model.db.repository.UserInfoRepo;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.dto.request.TeamInfoRequest;
import com.itmo.chgk.model.dto.response.TeamInfoResponse;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.CommonStatus;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepo teamRepo;
    private final UserInfoRepo userInfoRepo;
    private final UserRepo userRepo;
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
                    UserInfoResponse viceCaptain = mapper.convertValue(team.getViceCaptain(), UserInfoResponse.class);
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
        UserInfoResponse viceCaptain = team.getViceCaptain() == null ? null : mapper.convertValue(team.getViceCaptain(), UserInfoResponse.class);
        response.setCaptain(captain);
        response.setViceCaptain(viceCaptain);
        return response;
    }

    @Override
    public TeamInfoResponse createTeam(TeamInfoRequest request) {

        if (request.getTeamName() == null) {
            throw new CustomException("Необходимо указать название команды", HttpStatus.BAD_REQUEST);
        }

        if (teamRepo.existsByTeamName(request.getTeamName())) {
            throw new CustomException("Команда с таким названием уже существует", HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        UserInfo captain = userRepo.findById(user.getUsername()).get().getUserInfo();

        if (teamRepo.existsByCaptain(captain)) {
            throw new CustomException("Команда с таким капитаном уже существует", HttpStatus.BAD_REQUEST);
        }

        long viceId = request.getViceCaptainId();
        if (viceId != 0) {
            if (viceId == captain.getId()) {
                throw new CustomException("Создатель команды автоматически устанавливается в качестве капитана, " +
                        "поэтому не может быть также вице-капитаном", HttpStatus.BAD_REQUEST);
            }
            if (!userInfoRepo.existsById(viceId)) {
                throw new CustomException("Кандидат в вице-капитаны не зарегистрирован в БД", HttpStatus.BAD_REQUEST);
            }

            UserInfo vice = userInfoRepo.findById(viceId).get();
            if (teamRepo.existsByViceCaptain(vice)) {
                throw new CustomException("Кандидат в вице-капитаны уже выполняет аналогичную роль в другой команде", HttpStatus.BAD_REQUEST);
            }

            if (!vice.getLogin().isEnabled()) {
                throw new CustomException("Кандидат в вице-капитаны заблокирован/удален", HttpStatus.BAD_REQUEST);
            }
        }

        Team team = mapper.convertValue(request, Team.class);
        team.setTeamName(request.getTeamName());

        userService.addAuthority(captain.getLogin().getUsername(), List.of("ROLE_CAPTAIN"));
        userService.deleteAuthority(captain.getLogin().getUsername(), List.of("ROLE_USER"));
        team.setCaptain(captain);

        if (viceId != 0) {
            UserInfo vice = userInfoService.getUserDb(viceId);
            userService.addAuthority(vice.getLogin().getUsername(), List.of("ROLE_VICECAPTAIN"));
            userService.deleteAuthority(vice.getLogin().getUsername(), List.of("ROLE_USER"));
            team.setViceCaptain(vice);
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
        String captainName = captain.getLogin().getUsername();

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (team.getViceCaptain() == null) {
                if (!userName.equals(captainName)) {
                    throw new CustomException("Пользователь не имеет прав на редактирование данной команды", HttpStatus.FORBIDDEN);
                }
            } else {
                String viceName = team.getViceCaptain().getLogin().getUsername();
                if (!userName.equals(captainName) && !userName.equals(viceName)) {
                    throw new CustomException("Пользователь не имеет прав на редактирование данной команды", HttpStatus.FORBIDDEN);
                }
            }
        }

        if (request.getTeamName() != null && !request.getTeamName().equals(team.getTeamName()) &&  teamRepo.existsByTeamName(request.getTeamName())) {
            throw new CustomException("Команда с таким названием уже существует", HttpStatus.BAD_REQUEST);
        }

        long newCaptainId = request.getCaptainId();
        if (newCaptainId < 0) {
            throw new CustomException("Нельзя удалить капитана команды (только заменить на другого)", HttpStatus.BAD_REQUEST);
        }

        if (newCaptainId > 0) {
            if(!userInfoRepo.existsById(newCaptainId)) {
                throw new CustomException("Кандидат в капитаны не зарегистрирован в БД", HttpStatus.BAD_REQUEST);
            }

            UserInfo newCaptain = userInfoRepo.findById(newCaptainId).get();

            if (!newCaptain.getLogin().isEnabled()) {
                throw new CustomException("Кандидат в капитаны заблокирован/удален", HttpStatus.BAD_REQUEST);
            }

            if (newCaptainId!=team.getCaptain().getId() && teamRepo.existsByCaptain(newCaptain)) {
                throw new CustomException("Кандидат в капитаны уже является капитаном другой команды", HttpStatus.BAD_REQUEST);
            }

            if (team.getViceCaptain() != null && newCaptainId != team.getCaptain().getId() && userName.equals(team.getViceCaptain().getLogin().getUsername())) {
                throw new CustomException("Вице-капитан не может заменить капитана", HttpStatus.BAD_REQUEST);
            }
        }

        long newViceCaptainId = request.getViceCaptainId();
        if (newViceCaptainId > 0) {
            if(!userInfoRepo.existsById(newViceCaptainId)) {
                throw new CustomException("Кандидат в вице-капитаны не зарегистрирован в БД", HttpStatus.BAD_REQUEST);
            }

            UserInfo newViceCaptain = userInfoRepo.findById(newViceCaptainId).get();

            if (!newViceCaptain.getLogin().isEnabled()) {
                throw new CustomException("Кандидат в вице-капитаны заблокирован/удален", HttpStatus.BAD_REQUEST);
            }

            if (team.getViceCaptain()!=null){
                if (newViceCaptainId!=team.getViceCaptain().getId() && teamRepo.existsByViceCaptain(newViceCaptain)) {
                    throw new CustomException("Кандидат в вице-капитаны уже является вице-капитаном другой команды", HttpStatus.BAD_REQUEST);
                }
            } else if (teamRepo.existsByViceCaptain(newViceCaptain)) {
                throw new CustomException("Кандидат в вице-капитаны уже является вице-капитаном другой команды", HttpStatus.BAD_REQUEST);
            }

            if (newCaptainId == 0) {
                if (team.getCaptain().getId() == request.getViceCaptainId()) {
                    throw new CustomException("Нельзя установить вице-капитаном пользователя, который уже является капитаном команды", HttpStatus.BAD_REQUEST);
                }
            } else {
                if (request.getCaptainId() == request.getViceCaptainId()) {
                    throw new CustomException("Нельзя установить одного пользователя и капитаном и вице-капитаном команды", HttpStatus.BAD_REQUEST);
                }
            }

        }

        team.setTeamName(request.getTeamName() == null ? team.getTeamName() : request.getTeamName());

        if (request.getCaptainId() != 0 && request.getCaptainId()!=captain.getId()) {

            UserInfo newCaptainUI = userInfoService.getUserDb(request.getCaptainId());
            User newCaptain = userService.getUser(newCaptainUI.getLogin().getUsername());
            Collection<? extends GrantedAuthority> newCaptainAuthorities = newCaptain.getAuthorities();
            List<String> newCaptainListAuthorities = newCaptainAuthorities
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            if (newCaptainListAuthorities.contains("ROLE_CAPTAIN")) {
                throw new CustomException("Пользователь уже является капитаном другой команды", HttpStatus.BAD_REQUEST);
            }

            if(team.getViceCaptain() != null && request.getCaptainId()==team.getViceCaptain().getId()){
                userService.deleteAuthority(team.getViceCaptain().getLogin().getUsername(), List.of("ROLE_VICECAPTAIN"));
                team.setViceCaptain(null);
            }

            userService.deleteAuthority(team.getCaptain().getLogin().getUsername(),  List.of("ROLE_CAPTAIN"));
            userService.addAuthority(team.getCaptain().getLogin().getUsername(), List.of("ROLE_USER"));

            userService.deleteAuthority(newCaptain.getUsername(),  List.of("ROLE_USER"));
            userService.addAuthority(newCaptain.getUsername(), List.of("ROLE_CAPTAIN"));
            team.setCaptain(newCaptainUI);
        }

        if (request.getViceCaptainId() == -1) {
            if (team.getViceCaptain() != null) {
                UserInfo viceCaptainUI = team.getViceCaptain();
                userService.deleteAuthority(viceCaptainUI.getLogin().getUsername(),  List.of("ROLE_VICECAPTAIN"));
                userService.addAuthority(viceCaptainUI.getLogin().getUsername(), List.of("ROLE_USER"));
                team.setViceCaptain(null);
            }
        } else if (request.getViceCaptainId() > 0) {
            UserInfo newViceCaptainUI = userInfoService.getUserDb(request.getViceCaptainId());
            User newViceCaptain = userService.getUser(newViceCaptainUI.getLogin().getUsername());

            if (team.getViceCaptain() == null) {
                userService.addAuthority(newViceCaptain.getUsername(), List.of("ROLE_VICECAPTAIN"));
                userService.deleteAuthority(newViceCaptain.getUsername(),  List.of("ROLE_USER"));
                team.setViceCaptain(newViceCaptainUI);
            } else {
                if (team.getViceCaptain().getId() != request.getViceCaptainId()) {
                    UserInfo viceCaptainUI = team.getViceCaptain();
                    userService.deleteAuthority(viceCaptainUI.getLogin().getUsername(),  List.of("ROLE_VICECAPTAIN"));
                    userService.addAuthority(viceCaptainUI.getLogin().getUsername(), List.of("ROLE_USER"));

                    userService.addAuthority(newViceCaptainUI.getLogin().getUsername(), List.of("ROLE_VICECAPTAIN"));
                    userService.deleteAuthority(newViceCaptain.getUsername(),  List.of("ROLE_USER"));
                    team.setViceCaptain(newViceCaptainUI);
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

        if (team.getStatus()==CommonStatus.DELETED) {
            throw new CustomException("Команда уже удалена", HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userName = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfo captain = team.getCaptain();
        String captainName = captain.getLogin().getUsername();

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (team.getViceCaptain() == null) {
                if (!userName.equals(captainName)) {
                    throw new CustomException("Пользователь не имеет прав на удаление данной команды", HttpStatus.FORBIDDEN);
                }
            } else {
                String viceName = team.getViceCaptain().getLogin().getUsername();
                if (!userName.equals(captainName) && !userName.equals(viceName)) {
                    throw new CustomException("Пользователь не имеет прав на удаление данной команды", HttpStatus.FORBIDDEN);
                }
            }
        }

        userService.deleteAuthority(captainName,  List.of("ROLE_CAPTAIN"));
        userService.addAuthority(captainName, List.of("ROLE_USER"));

        if (team.getViceCaptain()!=null) {
            String viceName = team.getViceCaptain().getLogin().getUsername();
            userService.deleteAuthority(viceName,  List.of("ROLE_VICECAPTAIN"));
            userService.addAuthority(viceName, List.of("ROLE_USER"));
        }

        team.setCaptain(null);
        team.setViceCaptain(null);

        List<UserInfo> members = team.getUserInfos();
        for (UserInfo userInfo : members) {
            userInfo.setTeam(null);
            userInfoRepo.save(userInfo);
        }
        team.setUserInfos(new ArrayList<>());

        team.setUpdatedAt(LocalDateTime.now());
        team.setStatus(CommonStatus.DELETED);
        teamRepo.save(team);
    }

    @Override
    public Page<UserInfoResponse> setMember(Long teamId, Long userId, Integer page, Integer perPage, String sort, Sort.Direction order) {
        Team team = getTeamDb(teamId);

        if (team.getStatus()==CommonStatus.DELETED) {
            throw new CustomException("Нельзя добавить члена в удаленную команду", HttpStatus.FORBIDDEN);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userName = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfo captain = team.getCaptain();
        String captainName = captain.getLogin().getUsername();

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (team.getViceCaptain() == null) {
                if (!userName.equals(captainName)) {
                    throw new CustomException("Пользователь не имеет прав на изменение состава данной команды", HttpStatus.FORBIDDEN);
                }
            } else {
                String viceName = team.getViceCaptain().getLogin().getUsername();
                if (!userName.equals(captainName) && !userName.equals(viceName)) {
                    throw new CustomException("Пользователь не имеет прав на изменение состава данной команды", HttpStatus.FORBIDDEN);
                }
            }
        }

        UserInfo userInfo = userInfoService.getUserDb(userId);

        if (!userInfo.getLogin().isEnabled()) {
            throw new CustomException("Добавляемый пользователь заблокирован/удален", HttpStatus.BAD_REQUEST);
        }

        if (userInfo.getTeam()!=null) {
            throw new CustomException("Пользователь уже зарегистрирован в этой или другой команде", HttpStatus.BAD_REQUEST);
        }

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
        String captainName = captain.getLogin().getUsername();


        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (!userInfo.getLogin().getUsername().equals(userName)) {
                if (team.getViceCaptain() == null) {
                    if (!userName.equals(captainName)) {
                        throw new CustomException("Пользователь не имеет прав на изменение состава данной команды", HttpStatus.FORBIDDEN);
                    }
                } else {
                    String viceName = team.getViceCaptain().getLogin().getUsername();
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
