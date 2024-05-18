package com.itmo.chgk.security;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUser(String user);
    JwtAuthenticationResponse createUser(UserRequest request);
    User updateUser(UserRequest request);
    String delete(String user);
}
