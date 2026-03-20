package com.leyi.security;

import jakarta.servlet.http.HttpServletRequest;

public final class AuthContext {

    private AuthContext() {
    }

    public static String getUserType(HttpServletRequest request) {
        Object value = request.getAttribute("userType");
        return value == null ? null : value.toString();
    }

    public static Long getUserId(HttpServletRequest request) {
        return toLong(request.getAttribute("userId"));
    }

    public static Long getAdminId(HttpServletRequest request) {
        return toLong(request.getAttribute("adminId"));
    }

    public static String getPhone(HttpServletRequest request) {
        Object value = request.getAttribute("phone");
        return value == null ? null : value.toString();
    }

    public static String getRole(HttpServletRequest request) {
        Object value = request.getAttribute("role");
        return value == null ? null : value.toString();
    }

    public static boolean isUser(HttpServletRequest request) {
        return "user".equals(getUserType(request)) && getUserId(request) != null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(getUserType(request)) && getAdminId(request) != null;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
