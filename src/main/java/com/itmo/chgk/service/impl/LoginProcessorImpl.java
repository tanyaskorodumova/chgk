package com.itmo.chgk.service.impl;

import com.itmo.chgk.model.db.entity.Team;
import com.itmo.chgk.model.db.entity.Tournament;
import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.repository.TeamRepo;
import com.itmo.chgk.model.db.repository.TournamentRepo;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.enums.UserRole;
import com.itmo.chgk.service.LoggedUserManagementService;
import com.itmo.chgk.service.LoginProcessor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
@Getter
@Setter
public class LoginProcessorImpl implements LoginProcessor {
    private final UserRepo userRepo;
    private final TeamRepo teamRepo;
    private final TournamentRepo tournamentRepo;
    private final LoggedUserManagementService loggedUserManagementService;
    private String login;
    private String password;

    public boolean login() {
        String login = this.login;
        String password = this.password;

        boolean loggedIn = false;

        User user = userRepo.findByLoginAndPassword(login, password);

        if (user != null) {
            loggedIn = true;
            loggedUserManagementService.setUser(user);
            if(user.getRole().equals(UserRole.CAPTAIN)) {
                Team team = teamRepo.findFirstByCaptain(user);
                loggedUserManagementService.setTeamId(team == null ? null : team.getId());
            } else if (user.getRole().equals(UserRole.VICECAPTAIN)) {
                Team team = teamRepo.findFirstByViceCaptain(user);
                loggedUserManagementService.setTeamId(team == null ? null : team.getId());
            } else if (user.getRole().equals(UserRole.ORGANIZER)) {
                Tournament tournament = tournamentRepo.findFirstByOrganizer(user);
                loggedUserManagementService.setTournamentId(tournament == null ? null : tournament.getId());
            }
        }

        return loggedIn;
    }
}
