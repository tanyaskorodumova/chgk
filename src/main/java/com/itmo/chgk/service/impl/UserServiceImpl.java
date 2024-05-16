package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.UserDetail;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.dto.request.UserInfoRequest;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import com.itmo.chgk.model.enums.CommonStatus;
import com.itmo.chgk.model.enums.UserRole;
import com.itmo.chgk.service.LoggedUserManagementService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final ObjectMapper mapper;
    private final UserRepo userRepo;
    private final LoggedUserManagementService loggedUserManagementService;

    @Override
    public Page<UserInfoResponse> getAllUsers(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<UserInfoResponse> all = userRepo.findAllByStatusIsNot(request, CommonStatus.DELETED)
                .getContent()
                .stream()
                .map(user -> {
                    UserInfoResponse response = mapper.convertValue(user, UserInfoResponse.class);
                    response.setBirthDay(user.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    response.setPassword("Скрыто");
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public UserDetail getUserDb(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new CustomException("Пользователь не найден", HttpStatus.NOT_FOUND));
    }

    @Override
    public UserInfoResponse getUser(Long id) {
        UserDetail userDetail = getUserDb(id);
        UserInfoResponse userInfoResponse = mapper.convertValue(userDetail, UserInfoResponse.class);
        userInfoResponse.setPassword("Скрыто");
        userInfoResponse.setBirthDay(userDetail.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return userInfoResponse;
    }

    @Override
    public UserInfoResponse createUser(UserInfoRequest request) {
        if (loggedUserManagementService.getUserDetail() != null) {
            throw new CustomException("Для создания нового пользователя необходимо разлогиниться", HttpStatus.FORBIDDEN);
        }

        userRepo.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new CustomException("Данный email уже существует", HttpStatus.CONFLICT);
                });
        userRepo.findByLogin(request.getLogin())
                .ifPresent(user -> {
                    throw new CustomException("Данный логин уже существует", HttpStatus.CONFLICT);
                });

        LocalDate bDay = null;

        if (request.getBirthDay() == null) {
            throw new CustomException("Дата рождения должна быть укзаана", HttpStatus.BAD_REQUEST);
        }
        else {
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

        UserDetail userDetail = mapper.convertValue(request, UserDetail.class);
        userDetail.setBirthDate(bDay);
        userDetail.setRole(UserRole.USER);
        userDetail.setStatus(CommonStatus.CREATED);
        userDetail.setCreatedAt(LocalDateTime.now());

        userDetail = userRepo.save(userDetail);
        UserInfoResponse response = mapper.convertValue(userDetail, UserInfoResponse.class);
        response.setPassword("Скрыто");
        response.setBirthDay(userDetail.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return response;

    }

    @Override
    public UserInfoResponse updateUser(Long id, UserInfoRequest request) {
        if (loggedUserManagementService.getUserDetail() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (!loggedUserManagementService.getUserDetail().getRole().equals(UserRole.ADMIN) &&
                !loggedUserManagementService.getUserDetail().getId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на редактирование данного пользователя", HttpStatus.FORBIDDEN);
        }

        UserDetail userDetail = getUserDb(id);

        if (request.getEmail() != null && userRepo.findByEmail(request.getEmail()).isEmpty()) {
            userDetail.setEmail(request.getEmail());
        }
        else if (userRepo.findByEmail(request.getEmail()).isPresent() &&
                !userDetail.equals(userRepo.findByEmail(request.getEmail()).get())) {
            throw new CustomException("Данный email уже существует", HttpStatus.CONFLICT);
        }

        if (request.getLogin() != null && userRepo.findByLogin(request.getLogin()).isEmpty()) {
            userDetail.setLogin(request.getLogin());
        }
        else if (userRepo.findByLogin(request.getLogin()).isPresent() &&
                !userDetail.equals(userRepo.findByLogin(request.getLogin()).get())) {
            throw new CustomException("Данный логин уже существует", HttpStatus.CONFLICT);
        }

        userDetail.setPassword(request.getPassword() == null ? userDetail.getPassword() : request.getPassword());
        userDetail.setFirstName(request.getFirstName() == null? userDetail.getFirstName() : request.getFirstName());
        userDetail.setLastName(request.getLastName() == null ? userDetail.getLastName() : request.getLastName());

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
        userDetail.setBirthDate(bDay == null ? userDetail.getBirthDate() : bDay);

        userDetail.setStatus(CommonStatus.UPDATED);
        userDetail.setUpdatedAt(LocalDateTime.now());

        userDetail = userRepo.save(userDetail);
        UserInfoResponse response = mapper.convertValue(userDetail, UserInfoResponse.class);
        response.setPassword("Скрыто");
        response.setBirthDay(userDetail.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return response;
    }

    @Override
    public void deleteUser(Long id) {
        if (loggedUserManagementService.getUserDetail() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (!loggedUserManagementService.getUserDetail().getRole().equals(UserRole.ADMIN) &&
                !loggedUserManagementService.getUserDetail().getId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на удаление данного пользователя", HttpStatus.FORBIDDEN);
        }

        UserDetail userDetail = getUserDb(id);
        userDetail.setStatus(CommonStatus.DELETED);
        userDetail.setUpdatedAt(LocalDateTime.now());
        userRepo.save(userDetail);
    }

    public void setRole(Long id, UserRole role) {
        UserDetail userDetail = getUserDb(id);
        userDetail.setRole(role);
        userRepo.save(userDetail);
    }
}
