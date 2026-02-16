package com.leyi.service;

import com.leyi.entity.User;
import com.leyi.mapper.UserMapper;
import com.leyi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // 模拟验证码存储
    private static final ConcurrentHashMap<String, String> CODE_MAP = new ConcurrentHashMap<>();

    public void sendCode(String phone) {
        // 生成6位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        CODE_MAP.put(phone, code);
        // 实际项目中这里应该调用短信服务发送验证码
        System.out.println("验证码发送到 " + phone + ": " + code);
    }

    public Map<String, Object> login(String phone, String code) {
        // 验证码校验（开发环境可以用000000跳过）
        String savedCode = CODE_MAP.get(phone);
        if (!"000000".equals(code) && (savedCode == null || !savedCode.equals(code))) {
            throw new RuntimeException("验证码错误");
        }
        CODE_MAP.remove(phone);

        // 查找或创建用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setName("用户" + phone.substring(7));
            user.setAvatar("");
            userMapper.insert(user);
        } else {
            userMapper.updateLoginTime(user.getId());
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), phone, "user");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public User getById(Long id) {
        return userMapper.findById(id);
    }

    public void update(User user) {
        userMapper.update(user);
    }
}



