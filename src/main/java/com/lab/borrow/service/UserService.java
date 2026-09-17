package com.lab.borrow.service;

import com.lab.borrow.dto.UserCreateRequest;
import com.lab.borrow.dto.UserResponse;
import com.lab.borrow.entity.User;
import com.lab.borrow.entity.UserRole;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "用户不存在"
                ));
        return UserResponse.from(user);
    }

    public UserResponse getUserByStudentId(String studentId) {
        String normalizedStudentId = normalizeRequiredText(
                studentId,
                "学号/工号",
                20
        );
        User user = userRepository.findByStudentId(normalizedStudentId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "用户不存在"
                ));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }

        String studentId = normalizeRequiredText(
                request.studentId(),
                "学号/工号",
                20
        );
        String username = normalizeRequiredText(
                request.username(),
                "姓名",
                50
        );
        String password = normalizePassword(request.password());
        UserRole role = parseRole(request.role());

        if (userRepository.existsByStudentId(studentId)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "学号/工号已存在"
            );
        }

        User user = new User(
                studentId,
                username,
                passwordEncoder.encode(password),
                role
        );
        return UserResponse.from(userRepository.save(user));
    }

    private UserRole parseRole(Integer role) {
        if (role == null) {
            return UserRole.STUDENT;
        }

        try {
            UserRole userRole = UserRole.fromCode(role);
            if (userRole == null) {
                throw new IllegalArgumentException("角色不能为空");
            }
            return userRole;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户角色参数无效");
        }
    }

    private String normalizeRequiredText(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "不能为空");
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "不能为空");
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + "不能超过" + maxLength + "个字符"
            );
        }
        return normalized;
    }

    private String normalizePassword(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码不能为空");
        }
        if (value.length() < 6) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码不能少于6个字符");
        }
        if (value.length() > 72) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码不能超过72个字符");
        }
        return value;
    }
}
