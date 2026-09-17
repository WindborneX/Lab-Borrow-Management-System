package com.lab.borrow.service;

import com.lab.borrow.dto.EquipmentCreateRequest;
import com.lab.borrow.dto.EquipmentResponse;
import com.lab.borrow.entity.Equipment;
import com.lab.borrow.entity.EquipmentStatus;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.datasource.url="
                + "jdbc:sqlite:file:equipment-service-test?mode=memory&cache=shared"
                + "&busy_timeout=5000",
        "spring.datasource.hikari.maximum-pool-size=1"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EquipmentServiceTest {

    @Autowired
    private EquipmentRepository equipmentRepository;

    private EquipmentService equipmentService;

    @BeforeEach
    void setUp() {
        equipmentService = new EquipmentService(equipmentRepository);
    }

    @Test
    void createsAndRetrievesEquipment() {
        EquipmentResponse created = equipmentService.createEquipment(
                new EquipmentCreateRequest("Jetson Nano", "开发板", "竞赛用")
        );

        EquipmentResponse found = equipmentService.getEquipment(created.id());

        assertThat(found.name()).isEqualTo("Jetson Nano");
        assertThat(found.category()).isEqualTo("开发板");
        assertThat(found.status()).isEqualTo(EquipmentStatus.AVAILABLE.getCode());
        assertThat(found.createdAt()).isNotNull();
    }

    @Test
    void rejectsBlankEquipmentName() {
        assertThatThrownBy(() -> equipmentService.createEquipment(
                new EquipmentCreateRequest("  ", "开发板", null)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("设备名称不能为空");
    }

    @Test
    void filtersEquipmentByStatus() {
        equipmentRepository.save(new Equipment(
                "可用设备",
                "开发板",
                null,
                EquipmentStatus.AVAILABLE
        ));
        equipmentRepository.save(new Equipment(
                "停用设备",
                "开发板",
                null,
                EquipmentStatus.DISABLED
        ));

        List<EquipmentResponse> result = equipmentService.listEquipment(
                null,
                null,
                EquipmentStatus.DISABLED.getCode()
        );

        assertThat(result)
                .extracting(EquipmentResponse::name)
                .containsExactly("停用设备");
    }

    @Test
    void returnsNotFoundForMissingEquipment() {
        assertThatThrownBy(() -> equipmentService.getEquipment(999L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND)
                )
                .hasMessage("设备不存在");
    }
}
