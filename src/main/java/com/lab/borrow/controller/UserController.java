package com.lab.borrow.controller;

import com.lab.borrow.dto.Result;
import com.lab.borrow.dto.UserCreateRequest;
import com.lab.borrow.dto.UserResponse;
import com.lab.borrow.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Result<UserResponse> getUser(@PathVariable Long id) {
        return Result.success(userService.getUser(id));
    }

    @GetMapping
    public Result<UserResponse> getUserByStudentId(
            @RequestParam String studentId
    ) {
        return Result.success(userService.getUserByStudentId(studentId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<UserResponse> createUser(
            @RequestBody UserCreateRequest request
    ) {
        return Result.success(userService.createUser(request));
    }
}
