package com.lab.borrow.service;

import com.lab.borrow.dto.BorrowApplicationRequest;
import com.lab.borrow.dto.BorrowRecordResponse;
import com.lab.borrow.dto.ReturnEquipmentRequest;
import com.lab.borrow.entity.BorrowRecord;
import com.lab.borrow.entity.BorrowStatus;
import com.lab.borrow.entity.Equipment;
import com.lab.borrow.entity.EquipmentStatus;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.repository.BorrowRecordRepository;
import com.lab.borrow.repository.EquipmentRepository;
import com.lab.borrow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public BorrowService(
            BorrowRecordRepository borrowRecordRepository,
            EquipmentRepository equipmentRepository,
            UserRepository userRepository
    ) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BorrowRecordResponse createBorrowApplication(
            BorrowApplicationRequest request
    ) {
        validateRequest(request);

        Long equipmentId = request.equipmentId();
        Long userId = request.userId();

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "设备不存在"
                ));

        ensureEquipmentAvailable(equipment);

        int updated = equipmentRepository.updateStatusIfCurrent(
                equipmentId,
                EquipmentStatus.AVAILABLE,
                EquipmentStatus.BORROWED
        );
        if (updated == 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "设备已被借出"
            );
        }

        BorrowRecord record = new BorrowRecord(
                equipmentId,
                userId,
                request.expectReturnTime()
        );
        return BorrowRecordResponse.from(borrowRecordRepository.save(record));
    }

    @Transactional
    public BorrowRecordResponse returnEquipment(
            Long recordId,
            ReturnEquipmentRequest request
    ) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }
        if (request.userId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户ID不能为空");
        }

        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "借用记录不存在"
                ));

        if (!record.getUserId().equals(request.userId())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "只能归还本人借用的设备"
            );
        }

        LocalDateTime actualReturnTime = LocalDateTime.now();
        int recordUpdated = borrowRecordRepository.markReturnedIfBorrowing(
                recordId,
                actualReturnTime
        );
        if (recordUpdated == 0) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "该记录已处理，请勿重复操作"
            );
        }

        int equipmentUpdated = equipmentRepository.updateStatusIfCurrent(
                record.getEquipmentId(),
                EquipmentStatus.BORROWED,
                EquipmentStatus.AVAILABLE
        );
        if (equipmentUpdated == 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "设备状态异常，无法归还"
            );
        }

        BorrowRecord returnedRecord = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "借用记录不存在"
                ));
        return BorrowRecordResponse.from(returnedRecord);
    }

    public List<BorrowRecordResponse> listBorrowRecords(
            Long userId,
            Integer status
    ) {
        if (userId != null && userId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户ID参数无效");
        }

        BorrowStatus borrowStatus = parseBorrowStatus(status);
        return borrowRecordRepository
                .search(userId, borrowStatus)
                .stream()
                .map(BorrowRecordResponse::from)
                .toList();
    }

    private void validateRequest(BorrowApplicationRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }
        if (request.equipmentId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "设备ID不能为空");
        }
        if (request.userId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户ID不能为空");
        }
        if (
                request.expectReturnTime() != null
                        && !request.expectReturnTime().isAfter(LocalDateTime.now())
        ) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "预计归还时间必须晚于当前时间"
            );
        }
    }

    private void ensureEquipmentAvailable(Equipment equipment) {
        if (equipment.getStatus() == EquipmentStatus.BORROWED) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "设备已被借出"
            );
        }
        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "设备当前不可用"
            );
        }
    }

    private BorrowStatus parseBorrowStatus(Integer status) {
        try {
            return BorrowStatus.fromCode(status);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "借用状态参数无效");
        }
    }
}
