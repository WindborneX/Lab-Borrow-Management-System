package com.lab.borrow.entity;

public enum UserRole {
    STUDENT(0),
    ADMINISTRATOR(1);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserRole fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }

        throw new IllegalArgumentException("未知用户角色: " + code);
    }
}
