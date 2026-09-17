package com.lab.borrow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "borrow_time", nullable = false)
    private LocalDateTime borrowTime;

    @Column(name = "expect_return_time")
    private LocalDateTime expectReturnTime;

    @Column(name = "actual_return_time")
    private LocalDateTime actualReturnTime;

    @Convert(converter = BorrowStatusConverter.class)
    @Column(nullable = false)
    private BorrowStatus status;

    protected BorrowRecord() {
    }

    public BorrowRecord(
            Long equipmentId,
            Long userId,
            LocalDateTime expectReturnTime
    ) {
        this.equipmentId = equipmentId;
        this.userId = userId;
        this.expectReturnTime = expectReturnTime;
    }

    @PrePersist
    void initializeDefaults() {
        if (borrowTime == null) {
            borrowTime = LocalDateTime.now();
        }
        if (status == null) {
            status = BorrowStatus.BORROWING;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getBorrowTime() {
        return borrowTime;
    }

    public LocalDateTime getExpectReturnTime() {
        return expectReturnTime;
    }

    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }

    public BorrowStatus getStatus() {
        return status;
    }
}
