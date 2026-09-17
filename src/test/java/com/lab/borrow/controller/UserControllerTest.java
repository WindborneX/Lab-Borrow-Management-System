package com.lab.borrow.controller;

import com.lab.borrow.dto.UserResponse;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.exception.GlobalExceptionHandler;
import com.lab.borrow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createsUser() throws Exception {
        given(userService.createUser(any())).willReturn(userResponse());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "20260001",
                                  "username": "张三",
                                  "role": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studentId").value("20260001"));
    }

    @Test
    void retrievesUserByStudentId() throws Exception {
        given(userService.getUserByStudentId("20260001"))
                .willReturn(userResponse());

        mockMvc.perform(get("/api/v1/users")
                        .param("studentId", "20260001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("张三"));
    }

    @Test
    void returnsNotFoundForMissingUser() throws Exception {
        given(userService.getUser(99L))
                .willThrow(new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "20260001",
                "张三",
                0,
                LocalDateTime.of(2026, 9, 17, 16, 30)
        );
    }
}
