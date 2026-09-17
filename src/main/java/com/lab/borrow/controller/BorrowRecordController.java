package com.lab.borrow.controller;

import com.lab.borrow.dto.BorrowRecordResponse;
import com.lab.borrow.dto.Result;
import com.lab.borrow.dto.ReturnEquipmentRequest;
import com.lab.borrow.service.BorrowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrow-records")
public class BorrowRecordController {

    private final BorrowService borrowService;

    public BorrowRecordController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping
    public Result<List<BorrowRecordResponse>> listBorrowRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(
                borrowService.listBorrowRecords(userId, status)
        );
    }

    @PostMapping("/{id}/return")
    public Result<BorrowRecordResponse> returnEquipment(
            @PathVariable Long id,
            @RequestBody ReturnEquipmentRequest request
    ) {
        return Result.success(
                borrowService.returnEquipment(id, request)
        );
    }
}
