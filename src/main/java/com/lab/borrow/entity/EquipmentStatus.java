package com.lab.borrow.entity;

public enum EquipmentStatus {
    AVAILABLE(0),
    BORROWED(1),
    MAINTAINING(2),
    DISABLED(3);

    private final int code;

    EquipmentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static EquipmentStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (EquipmentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException("未知设备状态: " + code);
    }
}
