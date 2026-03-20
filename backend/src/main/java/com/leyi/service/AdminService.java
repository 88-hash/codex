package com.leyi.service;

import cn.hutool.crypto.digest.BCrypt;
import com.leyi.entity.Admin;
import com.leyi.exception.BusinessException;
import com.leyi.mapper.AdminMapper;
import com.leyi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> BUILT_IN_ADMIN_PHONES = Set.of("13800000001", "13800000002");
    private static final String BUILT_IN_DEFAULT_PASSWORD = "123456";
    private static final String LEGACY_BROKEN_HASH = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH";
    private static final String FIXED_HASH = "$2a$10$sq0JnxJBhl9HMviT57r7n.QyQ2JdjrHVgZ1pkxE/7CPZ0gXP3A80O";

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> login(String phone, String password) {
        Admin admin = adminMapper.findByPhone(phone);
        if (admin == null) {
            throw new BusinessException(400, "账号不存在");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException(400, "请输入密码");
        }

        if (!BCrypt.checkpw(password, admin.getPassword())) {
            if (canRepairBuiltInPassword(admin, password)) {
                adminMapper.updatePassword(admin.getId(), FIXED_HASH);
                admin.setPassword(FIXED_HASH);
            } else {
                throw new BusinessException(400, "密码错误");
            }
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
            throw new BusinessException(400, "手机号已存在");
        }
        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty()) {
            throw new BusinessException(400, "初始密码不能为空");
        }
        admin.setPassword(BCrypt.hashpw(admin.getPassword()));
        adminMapper.insert(admin);
    }

    public void update(Admin admin) {
        adminMapper.update(admin);
    }

    public void resetPassword(Long id, String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException(400, "密码不能为空");
        }
        adminMapper.updatePassword(id, BCrypt.hashpw(password));
    }

    public void delete(Long id) {
        adminMapper.delete(id);
    }

    private boolean canRepairBuiltInPassword(Admin admin, String password) {
        return BUILT_IN_ADMIN_PHONES.contains(admin.getPhone())
                && BUILT_IN_DEFAULT_PASSWORD.equals(password)
                && LEGACY_BROKEN_HASH.equals(admin.getPassword());
    }
}
