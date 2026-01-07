package com.smartcommunity.service;

import com.smartcommunity.entity.User;
import java.util.List;

public interface UserService {
    // 用户注册
    int register(User user);

    // 用户登录
    User login(String username, String password);

    // 根据用户名获取用户信息
    User getUserByUsername(String username);

    // 检查用户名是否已存在
    boolean existsByUsername(String username);

    // 检查手机号码是否已存在
    boolean existsByPhone(String phone);

    // 检查手机号码是否已存在（排除指定用户ID）
    boolean existsByPhone(String phone, int excludeUserId);

    // 检查邮箱是否已存在
    boolean existsByEmail(String email);

    // 检查邮箱是否已存在（排除指定用户ID）
    boolean existsByEmail(String email, int excludeUserId);

    // 更新用户信息
    int updateUser(User user);

    // 修改密码
    boolean changePassword(String username, String oldPassword, String newPassword);
    
    // 获取所有居民用户
    List<User> getResidents();
    
    // 根据条件获取居民用户
    List<User> getResidents(String searchKeyword, Integer status);
    
    // 添加新用户
    int addUser(User user);
    
    // 更新用户状态（禁用/启用）
    int updateUserStatus(int userId, int status);
    
    // 根据ID获取用户信息
    User getUserById(int userId);
    
    // 获取所有服务商用户
    List<User> getProviders();
    
    // 根据条件获取服务商用户
    List<User> getProviders(String searchKeyword, Integer status);
}