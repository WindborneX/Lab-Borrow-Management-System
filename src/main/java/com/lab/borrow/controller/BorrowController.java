package com.lab.borrow.controller;

import com.lab.borrow.dto.BorrowApplicationRequest;
import com.lab.borrow.dto.BorrowRecordResponse;
import com.lab.borrow.dto.Result;
import com.lab.borrow.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/borrow-applications")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<BorrowRecordResponse> createBorrowApplication(
            @RequestBody BorrowApplicationRequest request
    ) {
        return Result.success(
                borrowService.createBorrowApplication(request)
        );
    }
}
