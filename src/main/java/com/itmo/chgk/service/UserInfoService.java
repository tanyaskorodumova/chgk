package com.itmo.chgk.service;

import com.itmo.chgk.model.db.entity.UserInfo;
import com.itmo.chgk.model.dto.request.UserInfoRequest;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.UserInfoRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface UserInfoService {
    Page<UserInfoResponse> getAllUsers(Integer page, Integer perPage, String sort, Sort.Direction order);

    UserInfoResponse getUser(Long id);

    UserInfoResponse updateUser(Long id, UserInfoRequest request);

    UserInfo getUserDb(Long id);
}
