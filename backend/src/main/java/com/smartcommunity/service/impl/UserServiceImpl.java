package com.smartcommunity.service.impl;

import com.smartcommunity.dao.UserDAO;
import com.smartcommunity.dao.impl.UserDAOImpl;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.UserService;
import com.smartcommunity.util.PasswordUtil;
import java.util.List;

public class UserServiceImpl implements UserService {
    private UserDAO userDAO = new UserDAOImpl();

    @Override
    public int register(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && !user.getPhone().isEmpty() && existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && !user.getEmail().isEmpty() && existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 密码哈希处理
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));

        // 设置默认状态
        user.setStatus(1);

        // 将用户写入数据库
        return userDAO.addUser(user);
    }

    @Override
    public User login(String username, String password) {
        // 根据用户名获取用户
        System.out.println("Login attempt for username: " + username);
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            throw new RuntimeException("Invalid username or password");
        }

        System.out.println("Found user: " + user.getUsername());
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Input password: " + password);
        
        // 校验密码
        boolean passwordMatch = PasswordUtil.verifyPassword(password, user.getPassword());
        System.out.println("Password match: " + passwordMatch);
        
        if (!passwordMatch) {
            throw new RuntimeException("Invalid username or password");
        }

        // 校验用户状态
        if (user.getStatus() == 0) {
            System.out.println("User account is disabled: " + username);
            throw new RuntimeException("User account is disabled");
        }

        System.out.println("Login successful for user: " + username);
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userDAO.existsByUsername(username);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userDAO.existsByPhone(phone);
    }

    @Override
    public boolean existsByPhone(String phone, int excludeUserId) {
        return userDAO.existsByPhone(phone, excludeUserId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDAO.existsByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email, int excludeUserId) {
        return userDAO.existsByEmail(email, excludeUserId);
    }

    @Override
    public int updateUser(User user) {
        // 获取现有用户以保留未变更字段
        User existingUser = userDAO.getUserByUsername(user.getUsername());
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        // 仅更新非空字段
        if (user.getRealName() == null || user.getRealName().isEmpty()) {
            user.setRealName(existingUser.getRealName());
        }
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            user.setPhone(existingUser.getPhone());
        } else if (!user.getPhone().equals(existingUser.getPhone())) {
            // 检查手机号是否被其他用户占用（排除当前用户）
            if (existsByPhone(user.getPhone(), existingUser.getId())) {
                throw new RuntimeException("Phone number already exists");
            }
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            user.setEmail(existingUser.getEmail());
        } else if (!user.getEmail().equals(existingUser.getEmail())) {
            // 检查邮箱是否被其他用户占用（排除当前用户）
            if (existsByEmail(user.getEmail(), existingUser.getId())) {
                throw new RuntimeException("Email already exists");
            }
        }
        if (user.getAddress() == null || user.getAddress().isEmpty()) {
            user.setAddress(existingUser.getAddress());
        }
        if (user.getIdCard() == null || user.getIdCard().isEmpty()) {
            user.setIdCard(existingUser.getIdCard());
        }

        // 保留其他重要字段
        // 仅在用户对象未显式设置密码时保留原密码
        if (user.getPassword() == null) {
            user.setPassword(existingUser.getPassword());
        }
        user.setRole(existingUser.getRole());
        user.setStatus(existingUser.getStatus());
        user.setId(existingUser.getId());

        // 更新数据库中的用户
        return userDAO.updateUser(user);
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // 根据用户名获取用户
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 校验旧密码
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // 对新密码进行哈希并更新
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        // 直接更新用户，避免调用 updateUser 导致密码被覆盖
        int result = userDAO.updateUser(user);

        return result > 0;
    }
    
    @Override
    public List<User> getResidents() {
        return userDAO.getResidents();
    }
    
    @Override
    public List<User> getResidents(String searchKeyword, Integer status) {
        return userDAO.getResidents(searchKeyword, status);
    }
    
    @Override
    public int addUser(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && !user.getPhone().isEmpty() && existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && !user.getEmail().isEmpty() && existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 密码哈希处理
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));

        // 设置默认状态
        user.setStatus(1);

        // 将用户写入数据库
        return userDAO.addUser(user);
    }
    
    @Override
    public int updateUserStatus(int userId, int status) {
        return userDAO.updateUserStatus(userId, status);
    }
    
    @Override
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
    
    @Override
    public List<User> getProviders() {
        return userDAO.getProviders();
    }
    
    @Override
    public List<User> getProviders(String searchKeyword, Integer status) {
        return userDAO.getProviders(searchKeyword, status);
    }
}
