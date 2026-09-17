package com.lab.borrow.service;

import com.lab.borrow.dto.EquipmentCreateRequest;
import com.lab.borrow.dto.EquipmentResponse;
import com.lab.borrow.entity.Equipment;
import com.lab.borrow.entity.EquipmentStatus;
import com.lab.borrow.exception.BusinessException;
import com.lab.borrow.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<EquipmentResponse> listEquipment(
            String name,
            String category,
            Integer status
    ) {
        EquipmentStatus equipmentStatus = parseStatus(status);
        return equipmentRepository
                .search(
                        normalizeOptionalText(name),
                        normalizeOptionalText(category),
                        equipmentStatus
                )
                .stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    public EquipmentResponse getEquipment(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "设备不存在"
                ));
        return EquipmentResponse.from(equipment);
    }

    @Transactional
    public EquipmentResponse createEquipment(EquipmentCreateRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }

        String name = normalizeRequiredText(request.name(), "设备名称", 100);
        String category = normalizeOptionalText(request.category());
        String description = normalizeOptionalText(request.description());

        if (category != null && category.length() > 50) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "设备分类不能超过50个字符");
        }

        Equipment equipment = new Equipment(
                name,
                category,
                description,
                EquipmentStatus.AVAILABLE
        );
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    private EquipmentStatus parseStatus(Integer status) {
        try {
            return EquipmentStatus.fromCode(status);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "设备状态参数无效");
        }
    }

    private String normalizeRequiredText(String value, String fieldName, int maxLength) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "不能为空");
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + "不能超过" + maxLength + "个字符"
            );
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
