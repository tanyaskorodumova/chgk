package com.itmo.chgk.service;

import com.itmo.chgk.model.dto.response.JwtAuthenticationResponse;
import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.dto.request.UserRequest;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUser(String user);
    JwtAuthenticationResponse createUser(UserRequest request);
    User updateUser(UserRequest request);
    String deleteUser(String user);
}
