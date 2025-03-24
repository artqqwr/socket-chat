package com.artqqwr.socketchat.user;

public enum Role {
    ADMIN,
    USER;

    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return USER;
        }
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}
