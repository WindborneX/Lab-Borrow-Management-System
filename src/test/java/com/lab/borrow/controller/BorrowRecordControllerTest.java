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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BorrowRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowService borrowService;

    @Test
    void returnsEquipment() throws Exception {
        given(borrowService.returnEquipment(eq(1L), any()))
                .willReturn(returnedRecordResponse());

        mockMvc.perform(post("/api/v1/borrow-records/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    void rejectsReturnByAnotherUser() throws Exception {
        given(borrowService.returnEquipment(eq(1L), any()))
                .willThrow(new BusinessException(
                        HttpStatus.FORBIDDEN,
                        "只能归还本人借用的设备"
                ));

        mockMvc.perform(post("/api/v1/borrow-records/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 2
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("只能归还本人借用的设备"));
    }

    @Test
    void listsBorrowRecords() throws Exception {
        given(borrowService.listBorrowRecords(1L, 0))
                .willReturn(java.util.List.of(returnedRecordResponse()));

        mockMvc.perform(get("/api/v1/borrow-records")
                        .param("userId", "1")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    private BorrowRecordResponse returnedRecordResponse() {
        return new BorrowRecordResponse(
                1L,
                1L,
                1L,
                LocalDateTime.of(2026, 9, 17, 17, 0),
                null,
                LocalDateTime.of(2026, 9, 18, 10, 0),
                1
        );
    }
}
