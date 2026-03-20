package com.leyi.security;

import com.leyi.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthGuard {

    public Long requireUser(HttpServletRequest request) {
        if (!AuthContext.isUser(request)) {
            throw new BusinessException(403, "无权访问用户接口");
        }
        return AuthContext.getUserId(request);
    }

    public Long requireAdmin(HttpServletRequest request) {
        if (!AuthContext.isAdmin(request)) {
            throw new BusinessException(403, "无权访问后台接口");
        }
        return AuthContext.getAdminId(request);
    }

    public Long requireManager(HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        if (!"manager".equals(AuthContext.getRole(request))) {
            throw new BusinessException(403, "仅店长可操作");
        }
        return adminId;
    }

    public void requireOwner(boolean owned, String resourceName) {
        if (!owned) {
            throw new BusinessException(403, "无权访问" + resourceName);
        }
    }

    public void requirePositiveInt(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BusinessException(400, fieldName + "必须为正整数");
        }
    }

    public void requireBinaryFlag(Integer value, String fieldName) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BusinessException(400, fieldName + "参数非法");
        }
    }
}
