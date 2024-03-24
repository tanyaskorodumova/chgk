package com.itmo.chgk.controllers;

import com.itmo.chgk.model.dto.request.UserInfoRequest;
import com.itmo.chgk.model.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/all")
    public Page<UserInfoResponse> getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer perPage,
                                                           @RequestParam(defaultValue = "login") String sort,
                                                           @RequestParam(defaultValue = "ASC") Sort.Direction order)
    {
        //return questionService.getAllCars(page, perPage, sort, order);
        return null;
    }

    @GetMapping("/{id}")
    public UserInfoResponse getUser(@PathVariable Long id) {
        //return carService.getCar(id);
        return null;
    }

    @PostMapping("/new")
    public UserInfoResponse createUser(@RequestBody UserInfoRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public UserInfoResponse updateUser(@PathVariable Long id, @RequestBody UserInfoRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {

    }

}
