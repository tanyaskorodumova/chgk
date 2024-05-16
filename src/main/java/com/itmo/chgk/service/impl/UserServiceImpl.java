package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.UserD;
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
    public UserD getUserDb(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new CustomException("Пользователь не найден", HttpStatus.NOT_FOUND));
    }

    @Override
    public UserInfoResponse getUser(Long id) {
        UserD userD = getUserDb(id);
        UserInfoResponse userInfoResponse = mapper.convertValue(userD, UserInfoResponse.class);
        userInfoResponse.setPassword("Скрыто");
        userInfoResponse.setBirthDay(userD.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return userInfoResponse;
    }

    @Override
    public UserInfoResponse createUser(UserInfoRequest request) {
        if (loggedUserManagementService.getUserD() != null) {
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

        UserD userD = mapper.convertValue(request, UserD.class);
        userD.setBirthDate(bDay);
        userD.setRole(UserRole.USER);
        userD.setStatus(CommonStatus.CREATED);
        userD.setCreatedAt(LocalDateTime.now());

        userD = userRepo.save(userD);
        UserInfoResponse response = mapper.convertValue(userD, UserInfoResponse.class);
        response.setPassword("Скрыто");
        response.setBirthDay(userD.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return response;

    }

    @Override
    public UserInfoResponse updateUser(Long id, UserInfoRequest request) {
        if (loggedUserManagementService.getUserD() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (!loggedUserManagementService.getUserD().getRole().equals(UserRole.ADMIN) &&
                !loggedUserManagementService.getUserD().getId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на редактирование данного пользователя", HttpStatus.FORBIDDEN);
        }

        UserD userD = getUserDb(id);

        if (request.getEmail() != null && userRepo.findByEmail(request.getEmail()).isEmpty()) {
            userD.setEmail(request.getEmail());
        }
        else if (userRepo.findByEmail(request.getEmail()).isPresent() &&
                !userD.equals(userRepo.findByEmail(request.getEmail()).get())) {
            throw new CustomException("Данный email уже существует", HttpStatus.CONFLICT);
        }

        if (request.getLogin() != null && userRepo.findByLogin(request.getLogin()).isEmpty()) {
            userD.setLogin(request.getLogin());
        }
        else if (userRepo.findByLogin(request.getLogin()).isPresent() &&
                !userD.equals(userRepo.findByLogin(request.getLogin()).get())) {
            throw new CustomException("Данный логин уже существует", HttpStatus.CONFLICT);
        }

        userD.setPassword(request.getPassword() == null ? userD.getPassword() : request.getPassword());
        userD.setFirstName(request.getFirstName() == null? userD.getFirstName() : request.getFirstName());
        userD.setLastName(request.getLastName() == null ? userD.getLastName() : request.getLastName());

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
        userD.setBirthDate(bDay == null ? userD.getBirthDate() : bDay);

        userD.setStatus(CommonStatus.UPDATED);
        userD.setUpdatedAt(LocalDateTime.now());

        userD = userRepo.save(userD);
        UserInfoResponse response = mapper.convertValue(userD, UserInfoResponse.class);
        response.setPassword("Скрыто");
        response.setBirthDay(userD.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return response;
    }

    @Override
    public void deleteUser(Long id) {
        if (loggedUserManagementService.getUserD() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (!loggedUserManagementService.getUserD().getRole().equals(UserRole.ADMIN) &&
                !loggedUserManagementService.getUserD().getId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на удаление данного пользователя", HttpStatus.FORBIDDEN);
        }

        UserD userD = getUserDb(id);
        userD.setStatus(CommonStatus.DELETED);
        userD.setUpdatedAt(LocalDateTime.now());
        userRepo.save(userD);
    }

    public void setRole(Long id, UserRole role) {
        UserD userD = getUserDb(id);
        userD.setRole(role);
        userRepo.save(userD);
    }
}
