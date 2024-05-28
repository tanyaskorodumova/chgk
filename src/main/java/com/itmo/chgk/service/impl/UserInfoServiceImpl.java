package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.UserInfo;
import com.itmo.chgk.model.db.repository.UserInfoRepo;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.dto.request.UserInfoRequest;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.service.UserInfoService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoServiceImpl implements UserInfoService {
    private final ObjectMapper mapper;
    private final UserInfoRepo userInfoRepo;

    @Override
    public Page<UserInfoResponse> getAllUsers(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<UserInfoResponse> all = userInfoRepo.findAll(request)
                .getContent()
                .stream()
                .map(user -> {
                    UserInfoResponse response = mapper.convertValue(user, UserInfoResponse.class);
                    response.setBirthDay(user.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public UserInfo getUserDb(Long id) {
        return userInfoRepo.findById(id).orElseThrow(() -> new CustomException("Пользователь не найден", HttpStatus.NOT_FOUND));
    }

    @Override
    public UserInfoResponse getUser(Long id) {
        UserInfo userInfo = getUserDb(id);
        UserInfoResponse userInfoResponse = mapper.convertValue(userInfo, UserInfoResponse.class);
        userInfoResponse.setBirthDay(userInfo.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return userInfoResponse;
    }

    @Override
    public UserInfoResponse updateUser(Long id, UserInfoRequest request) {
        UserInfo userInfo = getUserDb(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            if (!userInfo.getLogin().getUsername().equals(user.getUsername())) {
                throw new CustomException("Пользователь не имеет прав на редактирование сведений о другом пользователе", HttpStatus.FORBIDDEN);
            }
        }

        if (request.getEmail() != null && userInfoRepo.findByEmail(request.getEmail()).isEmpty()) {
            userInfo.setEmail(request.getEmail());
        } else if (userInfoRepo.findByEmail(request.getEmail()).isPresent() &&
                !userInfo.equals(userInfoRepo.findByEmail(request.getEmail()).get())) {
            throw new CustomException("Данный email уже существует", HttpStatus.CONFLICT);
        }

        userInfo.setFirstName(request.getFirstName() == null? userInfo.getFirstName() : request.getFirstName());
        userInfo.setLastName(request.getLastName() == null ? userInfo.getLastName() : request.getLastName());

        LocalDate bDay = null;

        if (request.getBirthDay() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            try {
                bDay = LocalDate.parse(request.getBirthDay(), formatter);
                if (bDay.isAfter(LocalDate.now().minusYears(18))) {
                    throw new CustomException("Пользователь не может быть младше 18 лет", HttpStatus.BAD_REQUEST);
                }
            } catch (DateTimeParseException e) {
                throw new CustomException("Некорректная дата рождения", HttpStatus.BAD_REQUEST);
            }
        }
        userInfo.setBirthDate(bDay == null ? userInfo.getBirthDate() : bDay);

        userInfo.setUpdatedAt(LocalDateTime.now());

        userInfo = userInfoRepo.save(userInfo);
        UserInfoResponse response = mapper.convertValue(userInfo, UserInfoResponse.class);
        response.setBirthDay(userInfo.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return response;
    }
}
