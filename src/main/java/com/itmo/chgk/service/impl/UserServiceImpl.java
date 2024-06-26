package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.User;
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
    public User getUserDb(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new CustomException("Пользователь не найден", HttpStatus.NOT_FOUND));
    }

    @Override
    public UserInfoResponse getUser(Long id) {
        User user = getUserDb(id);
        UserInfoResponse userInfoResponse = mapper.convertValue(user, UserInfoResponse.class);
        userInfoResponse.setPassword("Скрыто");
        userInfoResponse.setBirthDay(user.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return userInfoResponse;
    }

    @Override
    public UserInfoResponse createUser(UserInfoRequest request) {
        if (loggedUserManagementService.getUser() != null) {
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

        User user = mapper.convertValue(request, User.class);
        user.setBirthDate(bDay);
        user.setRole(UserRole.USER);
        user.setStatus(CommonStatus.CREATED);
        user.setCreatedAt(LocalDateTime.now());

        user = userRepo.save(user);
        UserInfoResponse response = mapper.convertValue(user, UserInfoResponse.class);
        response.setPassword("Скрыто");
        response.setBirthDay(user.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return response;

    }

    @Override
    public UserInfoResponse updateUser(Long id, UserInfoRequest request) {
        if (loggedUserManagementService.getUser() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (!loggedUserManagementService.getUser().getRole().equals(UserRole.ADMIN) &&
                !loggedUserManagementService.getUser().getId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на редактирование данного пользователя", HttpStatus.FORBIDDEN);
        }

        User user = getUserDb(id);

        if (request.getEmail() != null && userRepo.findByEmail(request.getEmail()).isEmpty()) {
            user.setEmail(request.getEmail());
        }
        else if (userRepo.findByEmail(request.getEmail()).isPresent() &&
                !user.equals(userRepo.findByEmail(request.getEmail()).get())) {
            throw new CustomException("Данный email уже существует", HttpStatus.CONFLICT);
        }

        if (request.getLogin() != null && userRepo.findByLogin(request.getLogin()).isEmpty()) {
            user.setLogin(request.getLogin());
        }
        else if (userRepo.findByLogin(request.getLogin()).isPresent() &&
                !user.equals(userRepo.findByLogin(request.getLogin()).get())) {
            throw new CustomException("Данный логин уже существует", HttpStatus.CONFLICT);
        }

        user.setPassword(request.getPassword() == null ? user.getPassword() : request.getPassword());
        user.setFirstName(request.getFirstName() == null? user.getFirstName() : request.getFirstName());
        user.setLastName(request.getLastName() == null ? user.getLastName() : request.getLastName());

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
        user.setBirthDate(bDay == null ? user.getBirthDate() : bDay);

        user.setStatus(CommonStatus.UPDATED);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepo.save(user);
        UserInfoResponse response = mapper.convertValue(user, UserInfoResponse.class);
        response.setPassword("Скрыто");
        response.setBirthDay(user.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return response;
    }

    @Override
    public void deleteUser(Long id) {
        if (loggedUserManagementService.getUser() == null) {
            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
        } else if (!loggedUserManagementService.getUser().getRole().equals(UserRole.ADMIN) &&
                !loggedUserManagementService.getUser().getId().equals(id)) {
            throw new CustomException("Пользователь не имеет прав на удаление данного пользователя", HttpStatus.FORBIDDEN);
        }

        User user = getUserDb(id);
        user.setStatus(CommonStatus.DELETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
    }

    public void setRole(Long id, UserRole role) {
        User user = getUserDb(id);
        user.setRole(role);
        userRepo.save(user);
    }
}
