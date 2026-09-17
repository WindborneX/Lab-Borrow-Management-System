package com.lab.borrow.service;

import com.lab.borrow.dto.BorrowApplicationRequest;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url="
                + "jdbc:sqlite:file:borrow-concurrency?mode=memory&cache=shared"
                + "&busy_timeout=5000",
        "spring.datasource.hikari.maximum-pool-size=1"
})
class BorrowConcurrencyTest {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowService borrowService;

    @BeforeEach
    void cleanDatabase() {
        borrowRecordRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void onlyOneConcurrentBorrowApplicationSucceeds() throws Exception {
        User user = userRepository.saveAndFlush(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.saveAndFlush(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.AVAILABLE
        ));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        BorrowApplicationRequest request = new BorrowApplicationRequest(
                equipment.getId(),
                user.getId(),
                null
        );

        Callable<BorrowAttempt> attempt = () -> {
            ready.countDown();
            start.await();
            try {
                borrowService.createBorrowApplication(request);
                return new BorrowAttempt(true, null);
            } catch (BusinessException exception) {
                return new BorrowAttempt(false, exception.getStatus());
            }
        };

        try {
            Future<BorrowAttempt> first = executor.submit(attempt);
            Future<BorrowAttempt> second = executor.submit(attempt);
            ready.await();
            start.countDown();

            List<BorrowAttempt> results = List.of(first.get(), second.get());

            assertThat(results)
                    .filteredOn(BorrowAttempt::success)
                    .hasSize(1);
            assertThat(results)
                    .filteredOn(result -> !result.success())
                    .singleElement()
                    .extracting(BorrowAttempt::conflictStatus)
                    .isEqualTo(HttpStatus.CONFLICT);
        } finally {
            executor.shutdownNow();
        }

        assertThat(equipmentRepository.findById(equipment.getId()))
                .get()
                .extracting(Equipment::getStatus)
                .isEqualTo(EquipmentStatus.BORROWED);
        assertThat(borrowRecordRepository.findAll())
                .singleElement()
                .extracting(record -> record.getStatus())
                .isEqualTo(BorrowStatus.BORROWING);
    }

    @Test
    void onlyOneConcurrentReturnSucceeds() throws Exception {
        User user = userRepository.saveAndFlush(new User(
                "20260001",
                "张三",
                UserRole.STUDENT
        ));
        Equipment equipment = equipmentRepository.saveAndFlush(new Equipment(
                "Jetson Nano",
                "开发板",
                null,
                EquipmentStatus.BORROWED
        ));
        BorrowRecord record = borrowRecordRepository.saveAndFlush(new BorrowRecord(
                equipment.getId(),
                user.getId(),
                null
        ));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ReturnEquipmentRequest request = new ReturnEquipmentRequest(user.getId());

        Callable<BorrowAttempt> attempt = () -> {
            ready.countDown();
            start.await();
            try {
                borrowService.returnEquipment(record.getId(), request);
                return new BorrowAttempt(true, null);
            } catch (BusinessException exception) {
                return new BorrowAttempt(false, exception.getStatus());
            }
        };

        try {
            Future<BorrowAttempt> first = executor.submit(attempt);
            Future<BorrowAttempt> second = executor.submit(attempt);
            ready.await();
            start.countDown();

            List<BorrowAttempt> results = List.of(first.get(), second.get());

            assertThat(results)
                    .filteredOn(BorrowAttempt::success)
                    .hasSize(1);
            assertThat(results)
                    .filteredOn(result -> !result.success())
                    .singleElement()
                    .extracting(BorrowAttempt::conflictStatus)
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        } finally {
            executor.shutdownNow();
        }

        assertThat(equipmentRepository.findById(equipment.getId()))
                .get()
                .extracting(Equipment::getStatus)
                .isEqualTo(EquipmentStatus.AVAILABLE);
        assertThat(borrowRecordRepository.findById(record.getId()))
                .get()
                .extracting(BorrowRecord::getStatus)
                .isEqualTo(BorrowStatus.RETURNED);
    }

    private record BorrowAttempt(
            boolean success,
            HttpStatus conflictStatus
    ) {
    }
}
