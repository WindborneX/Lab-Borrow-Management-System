package com.lab.borrow.service;

import com.lab.borrow.dto.BorrowApplicationRequest;
import com.lab.borrow.dto.BorrowRecordResponse;
import com.lab.borrow.dto.ReturnEquipmentRequest;
import com.lab.borrow.entity.BorrowRecord;
import com.lab.borrow.entity.BorrowStatus;
import com.lab.borrow.entity.Equipment;
import com.lab.borrow.entity.EquipmentStatus;
import com.lab.borrow.entity.User;
import com.lab.borrow.entity.UserRole;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.repository.BorrowRecordRepository;
import com.lab.borrow.repository.EquipmentRepository;
import com.lab.borrow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BorrowServiceTest {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        borrowService = new BorrowService(
                borrowRecordRepository,
                equipmentRepository,
                userRepository
        );
    }

    @Test
    void createsBorrowRecordAndLocksEquipment() {
        User user = userRepository.save(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.save(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.AVAILABLE
        ));

        BorrowRecordResponse response = borrowService.createBorrowApplication(
                new BorrowApplicationRequest(
                        equipment.getId(),
                        user.getId(),
                        LocalDateTime.now().plusDays(2)
                )
        );

        assertThat(response.status()).isEqualTo(BorrowStatus.BORROWING.getCode());
        assertThat(response.borrowTime()).isNotNull();
        assertThat(equipmentRepository.findById(equipment.getId()))
                .get()
                .extracting(Equipment::getStatus)
                .isEqualTo(EquipmentStatus.BORROWED);
    }

    @Test
    void rejectsMissingUser() {
        Equipment equipment = equipmentRepository.save(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.AVAILABLE
        ));

        assertThatThrownBy(() -> borrowService.createBorrowApplication(
                new BorrowApplicationRequest(equipment.getId(), 999L, null)
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND)
                )
                .hasMessage("用户不存在");
    }

    @Test
    void rejectsMissingEquipment() {
        User user = userRepository.save(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));

        assertThatThrownBy(() -> borrowService.createBorrowApplication(
                new BorrowApplicationRequest(999L, user.getId(), null)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("设备不存在");
    }

    @Test
    void rejectsUnavailableEquipment() {
        User user = userRepository.save(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.save(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.MAINTAINING
        ));

        assertThatThrownBy(() -> borrowService.createBorrowApplication(
                new BorrowApplicationRequest(equipment.getId(), user.getId(), null)
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN)
                )
                .hasMessage("设备当前不可用");
    }

    @Test
    void rejectsPastExpectedReturnTime() {
        User user = userRepository.save(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.save(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.AVAILABLE
        ));

        assertThatThrownBy(() -> borrowService.createBorrowApplication(
                new BorrowApplicationRequest(
                        equipment.getId(),
                        user.getId(),
                        LocalDateTime.now().minusMinutes(1)
                )
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预计归还时间必须晚于当前时间");
    }

    @Test
    void returnsEquipmentAndReleasesIt() {
        User user = userRepository.save(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.save(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.BORROWED
        ));
        BorrowRecord record = borrowRecordRepository.save(new BorrowRecord(
                equipment.getId(),
                user.getId(),
                null
        ));

        BorrowRecordResponse response = borrowService.returnEquipment(
                record.getId(),
                new ReturnEquipmentRequest(user.getId())
        );

        assertThat(response.status()).isEqualTo(BorrowStatus.RETURNED.getCode());
        assertThat(response.actualReturnTime()).isNotNull();
        assertThat(equipmentRepository.findById(equipment.getId()))
                .get()
                .extracting(Equipment::getStatus)
                .isEqualTo(EquipmentStatus.AVAILABLE);
    }

    @Test
    void rejectsReturnByAnotherUser() {
        User borrower = userRepository.save(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        User other = userRepository.save(new User(
                "20260002",
                "李四",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.save(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.BORROWED
        ));
        BorrowRecord record = borrowRecordRepository.save(new BorrowRecord(
                equipment.getId(),
                borrower.getId(),
                null
        ));

        assertThatThrownBy(() -> borrowService.returnEquipment(
                record.getId(),
                new ReturnEquipmentRequest(other.getId())
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN)
                )
                .hasMessage("只能归还本人借用的设备");
    }

    @Test
    void rejectsDuplicateReturn() {
        User user = userRepository.save(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.save(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.BORROWED
        ));
        BorrowRecord record = borrowRecordRepository.save(new BorrowRecord(
                equipment.getId(),
                user.getId(),
                null
        ));
        ReturnEquipmentRequest request = new ReturnEquipmentRequest(user.getId());

        borrowService.returnEquipment(record.getId(), request);

        assertThatThrownBy(() -> borrowService.returnEquipment(record.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该记录已处理，请勿重复操作");
    }

    @Test
    void rejectsReturnForMissingRecord() {
        assertThatThrownBy(() -> borrowService.returnEquipment(
                999L,
                new ReturnEquipmentRequest(1L)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("借用记录不存在");
    }
}
