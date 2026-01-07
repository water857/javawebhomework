package com.smartcommunity.dao;

import com.smartcommunity.entity.User;
import java.util.List;

public interface UserDAO {
    // 添加用户
    int addUser(User user);

    // 根据用户名查询用户
    User getUserByUsername(String username);

    // 根据ID查询用户
    User getUserById(int id);

    // 更新用户信息
    int updateUser(User user);

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
    
    // 获取所有居民用户
    List<User> getResidents();
    
    // 根据条件获取居民用户
    List<User> getResidents(String searchKeyword, Integer status);
    
    // 更新用户状态（禁用/启用）
    int updateUserStatus(int userId, int status);
    
    // 获取所有服务商用户
    List<User> getProviders();
    
    // 根据条件获取服务商用户
    List<User> getProviders(String searchKeyword, Integer status);
}