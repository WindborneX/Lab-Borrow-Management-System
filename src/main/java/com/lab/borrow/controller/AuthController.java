package com.lab.borrow.controller;

import com.lab.borrow.dto.AuthUserResponse;
import com.lab.borrow.dto.LoginRequest;
import com.lab.borrow.dto.Result;
import com.lab.borrow.entity.User;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public Result<AuthUserResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        if (request == null || request.studentId() == null || request.studentId().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "学号/工号不能为空");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码不能为空");
        }

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.studentId().trim(),
                        request.password()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByStudentId(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "用户不存在"
                ));
        return Result.success(AuthUserResponse.from(user));
    }

    @GetMapping("/me")
    public Result<AuthUserResponse> currentUser(Authentication authentication) {
        User user = userRepository.findByStudentId(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "用户不存在"
                ));
        return Result.success(AuthUserResponse.from(user));
    }
}
