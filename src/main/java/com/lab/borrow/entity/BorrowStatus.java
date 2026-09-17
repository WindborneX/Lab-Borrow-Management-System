package com.lab.borrow.entity;

public enum BorrowStatus {
    BORROWING(0),
    RETURNED(1),
    OVERDUE(2);

    private final int code;

    BorrowStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BorrowStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (BorrowStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException("未知借用状态: " + code);
    }
}
