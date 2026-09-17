package com.lab.borrow.controller;

import com.lab.borrow.dto.BorrowRecordResponse;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.exception.GlobalExceptionHandler;
import com.lab.borrow.service.BorrowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowService borrowService;

    @Test
    void createsBorrowApplication() throws Exception {
        given(borrowService.createBorrowApplication(any()))
                .willReturn(borrowRecordResponse());

        mockMvc.perform(post("/api/v1/borrow-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": 1,
                                  "userId": 1,
                                  "expectReturnTime": "2026-09-20 18:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void returnsConflictWhenEquipmentIsAlreadyBorrowed() throws Exception {
        given(borrowService.createBorrowApplication(any()))
                .willThrow(new BusinessException(
                        HttpStatus.CONFLICT,
                        "设备已被借出"
                ));

        mockMvc.perform(post("/api/v1/borrow-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": 1,
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("设备已被借出"));
    }

    private BorrowRecordResponse borrowRecordResponse() {
        return new BorrowRecordResponse(
                1L,
                1L,
                1L,
                LocalDateTime.of(2026, 9, 17, 17, 0),
                LocalDateTime.of(2026, 9, 20, 18, 0),
                null,
                0
        );
    }
}
