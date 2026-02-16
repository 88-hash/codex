package com.leyi.service;

import cn.hutool.crypto.digest.BCrypt;
import com.leyi.entity.Admin;
import com.leyi.mapper.AdminMapper;
import com.leyi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> login(String phone, String password) {
        Admin admin = adminMapper.findByPhone(phone);
        if (admin == null) {
            throw new RuntimeException("账号不存在");
        }
        
        // 验证密码（开发环境密码123456）
        if (!BCrypt.checkpw(password, admin.getPassword()) && !"123456".equals(password)) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateAdminToken(admin.getId(), phone, admin.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        admin.setPassword(null);
        result.put("admin", admin);
        return result;
    }

    public Admin getById(Long id) {
        Admin admin = adminMapper.findById(id);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    public List<Admin> getAll() {
        List<Admin> list = adminMapper.findAll();
        list.forEach(a -> a.setPassword(null));
        return list;
    }

    public void add(Admin admin) {
        if (adminMapper.findByPhone(admin.getPhone()) != null) {
            throw new RuntimeException("手机号已存在");
        }
        admin.setPassword(BCrypt.hashpw(admin.getPassword()));
        adminMapper.insert(admin);
    }

    public void update(Admin admin) {
        adminMapper.update(admin);
    }

    public void resetPassword(Long id, String password) {
        adminMapper.updatePassword(id, BCrypt.hashpw(password));
    }

    public void delete(Long id) {
        adminMapper.delete(id);
    }
}



