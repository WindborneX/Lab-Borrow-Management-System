package com.lab.borrow.service;

import com.lab.borrow.dto.UserCreateRequest;
import com.lab.borrow.dto.UserResponse;
import com.lab.borrow.entity.UserRole;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void createsAndRetrievesUserById() {
        UserResponse created = userService.createUser(
                new UserCreateRequest("20260001", "张三", 0)
        );

        UserResponse found = userService.getUser(created.id());

        assertThat(found.studentId()).isEqualTo("20260001");
        assertThat(found.username()).isEqualTo("张三");
        assertThat(found.role()).isEqualTo(UserRole.STUDENT.getCode());
        assertThat(found.createdAt()).isNotNull();
    }

    @Test
    void retrievesUserByStudentId() {
        userService.createUser(new UserCreateRequest("ADMIN001", "管理员", 1));

        UserResponse found = userService.getUserByStudentId("ADMIN001");

        assertThat(found.username()).isEqualTo("管理员");
        assertThat(found.role()).isEqualTo(UserRole.ADMINISTRATOR.getCode());
    }

    @Test
    void rejectsDuplicateStudentId() {
        userService.createUser(new UserCreateRequest("20260001", "张三", 0));

        assertThatThrownBy(() -> userService.createUser(
                new UserCreateRequest("20260001", "李四", 0)
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT)
                )
                .hasMessage("学号/工号已存在");
    }

    @Test
    void rejectsInvalidRole() {
        assertThatThrownBy(() -> userService.createUser(
                new UserCreateRequest("20260002", "李四", 9)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户角色参数无效");
    }

    @Test
    void rejectsBlankUsername() {
        assertThatThrownBy(() -> userService.createUser(
                new UserCreateRequest("20260003", "  ", 0)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("姓名不能为空");
    }
}
