package com.lab.borrow.controller;

import com.lab.borrow.entity.Equipment;
import com.lab.borrow.entity.EquipmentStatus;
import com.lab.borrow.entity.User;
import com.lab.borrow.entity.UserRole;
import com.lab.borrow.repository.BorrowRecordRepository;
import com.lab.borrow.repository.EquipmentRepository;
import com.lab.borrow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url="
                + "jdbc:sqlite:file:auth-integration?mode=memory&cache=shared"
                + "&busy_timeout=5000",
        "spring.datasource.hikari.maximum-pool-size=1"
})
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        borrowRecordRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void logsInAndReturnsCurrentUser() throws Exception {
        createUser("20260001", "张三", "123456", UserRole.STUDENT);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "20260001",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("张三"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value("20260001"))
                .andExpect(jsonPath("$.data.role").value(0));
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        createUser("20260001", "张三", "123456", UserRole.STUDENT);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "20260001",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("学号/工号或密码错误"));
    }

    @Test
    void restrictsEquipmentCreationToAdministrator() throws Exception {
        createUser("20260001", "张三", "123456", UserRole.STUDENT);
        MockHttpSession studentSession = login("20260001", "123456");

        mockMvc.perform(post("/api/v1/equipment")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "测试设备",
                                  "category": "测试"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        createUser("ADMIN001", "管理员", "123456", UserRole.ADMINISTRATOR);
        MockHttpSession adminSession = login("ADMIN001", "123456");

        mockMvc.perform(post("/api/v1/equipment")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "管理员创建设备",
                                  "category": "测试"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("管理员创建设备"));
    }

    @Test
    void logsOutAndClearsSession() throws Exception {
        createUser("20260001", "张三", "123456", UserRole.STUDENT);
        MockHttpSession session = login("20260001", "123456");

        mockMvc.perform(post("/api/v1/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    private User createUser(
            String studentId,
            String username,
            String password,
            UserRole role
    ) {
        return userRepository.saveAndFlush(new User(
                studentId,
                username,
                passwordEncoder.encode(password),
                role
        ));
    }

    private MockHttpSession login(String studentId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "%s",
                                  "password": "%s"
                                }
                                """.formatted(studentId, password)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
