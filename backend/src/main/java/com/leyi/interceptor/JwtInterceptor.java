package com.leyi.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyi.common.Result;
import com.leyi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                request.setAttribute("userId", jwtUtil.getUserId(token));
                request.setAttribute("adminId", jwtUtil.getAdminId(token));
                request.setAttribute("phone", jwtUtil.getPhone(token));
                request.setAttribute("userType", jwtUtil.getUserType(token));
                request.setAttribute("role", jwtUtil.getRole(token));
                return true;
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(Result.error(401, "请先登录")));
        return false;
    }
}



