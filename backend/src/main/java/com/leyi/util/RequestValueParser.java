package com.leyi.util;

import com.leyi.exception.BusinessException;

import java.math.BigDecimal;
import java.util.Map;

public final class RequestValueParser {

    private RequestValueParser() {
    }

    public static String requireString(Map<String, ?> params, String key, String fieldName) {
        Object value = params.get(key);
        if (value == null) {
            throw new BusinessException(400, fieldName + "不能为空");
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            throw new BusinessException(400, fieldName + "不能为空");
        }
        return text;
    }

    public static String getTrimmedString(Map<String, ?> params, String key) {
        Object value = params.get(key);
        return value == null ? null : value.toString().trim();
    }

    public static Long requireLong(Map<String, ?> params, String key, String fieldName) {
        String text = requireString(params, key, fieldName);
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            throw new BusinessException(400, fieldName + "格式错误");
        }
    }

    public static Integer requireInteger(Map<String, ?> params, String key, String fieldName) {
        String text = requireString(params, key, fieldName);
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw new BusinessException(400, fieldName + "格式错误");
        }
    }

    public static Integer getInteger(Map<String, ?> params, String key, Integer defaultValue, String fieldName) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw new BusinessException(400, fieldName + "格式错误");
        }
    }

    public static BigDecimal requireBigDecimal(Map<String, ?> params, String key, String fieldName) {
        String text = requireString(params, key, fieldName);
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new BusinessException(400, fieldName + "格式错误");
        }
    }
}
