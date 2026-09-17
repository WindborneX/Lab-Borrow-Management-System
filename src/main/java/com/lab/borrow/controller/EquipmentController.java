package com.lab.borrow.controller;

import com.lab.borrow.dto.EquipmentCreateRequest;
import com.lab.borrow.dto.EquipmentResponse;
import com.lab.borrow.dto.Result;
import com.lab.borrow.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public Result<List<EquipmentResponse>> listEquipment(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(
                equipmentService.listEquipment(name, category, status)
        );
    }

    @GetMapping("/{id}")
    public Result<EquipmentResponse> getEquipment(@PathVariable Long id) {
        return Result.success(equipmentService.getEquipment(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<EquipmentResponse> createEquipment(
            @RequestBody EquipmentCreateRequest request
    ) {
        return Result.success(equipmentService.createEquipment(request));
    }
}
