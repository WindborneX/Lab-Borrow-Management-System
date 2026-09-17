package com.lab.borrow.controller;

import com.lab.borrow.dto.EquipmentResponse;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.exception.GlobalExceptionHandler;
import com.lab.borrow.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentController.class)
@Import(GlobalExceptionHandler.class)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentService equipmentService;

    @Test
    void listsEquipment() throws Exception {
        given(equipmentService.listEquipment(null, null, null))
                .willReturn(List.of(equipmentResponse()));

        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Jetson Nano"));
    }

    @Test
    void createsEquipment() throws Exception {
        given(equipmentService.createEquipment(any()))
                .willReturn(equipmentResponse());

        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jetson Nano",
                                  "category": "开发板",
                                  "description": "竞赛用"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void returnsNotFoundForMissingEquipment() throws Exception {
        given(equipmentService.getEquipment(99L))
                .willThrow(new BusinessException(HttpStatus.NOT_FOUND, "设备不存在"));

        mockMvc.perform(get("/api/v1/equipment/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("设备不存在"));
    }

    private EquipmentResponse equipmentResponse() {
        return new EquipmentResponse(
                1L,
                "Jetson Nano",
                "开发板",
                0,
                "竞赛用",
                LocalDateTime.of(2026, 9, 17, 16, 0)
        );
    }
}
