package com.itmo.chgk.service;

import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.dto.request.UserInfoRequest;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface UserService {
    Page<UserInfoResponse> getAllUsers(Integer page, Integer perPage, String sort, Sort.Direction order);

    UserInfoResponse getUser(Long id);

    UserInfoResponse createUser(UserInfoRequest request);

    UserInfoResponse updateUser(Long id, UserInfoRequest request);

    void deleteUser(Long id);

    User getUserDb(Long id);

    void setRole(Long id, UserRole role);
}
