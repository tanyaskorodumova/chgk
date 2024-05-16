package com.itmo.chgk.service;

import com.itmo.chgk.model.db.entity.UserDetail;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
@Getter
@Setter
public class LoggedUserManagementService {
    private UserDetail userDetail;
    private Long teamId;
    private Long tournamentId;
}
