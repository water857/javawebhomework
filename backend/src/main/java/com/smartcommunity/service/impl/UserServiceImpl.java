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
        // Check if username already exists
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if phone already exists
        if (user.getPhone() != null && !user.getPhone().isEmpty() && existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // Check if email already exists
        if (user.getEmail() != null && !user.getEmail().isEmpty() && existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Hash password
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));

        // Set default status
        user.setStatus(1);

        // Add user to database
        return userDAO.addUser(user);
    }

    @Override
    public User login(String username, String password) {
        // Get user by username
        System.out.println("Login attempt for username: " + username);
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            throw new RuntimeException("Invalid username or password");
        }

        System.out.println("Found user: " + user.getUsername());
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Input password: " + password);
        
        // Check password
        boolean passwordMatch = PasswordUtil.verifyPassword(password, user.getPassword());
        System.out.println("Password match: " + passwordMatch);
        
        if (!passwordMatch) {
            throw new RuntimeException("Invalid username or password");
        }

        // Check user status
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
        // Get existing user to preserve unchanged fields
        User existingUser = userDAO.getUserByUsername(user.getUsername());
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        // Only update fields that are not null or empty
        if (user.getRealName() == null || user.getRealName().isEmpty()) {
            user.setRealName(existingUser.getRealName());
        }
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            user.setPhone(existingUser.getPhone());
        } else if (!user.getPhone().equals(existingUser.getPhone())) {
            // Check if phone already exists for other user (exclude current user)
            if (existsByPhone(user.getPhone(), existingUser.getId())) {
                throw new RuntimeException("Phone number already exists");
            }
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            user.setEmail(existingUser.getEmail());
        } else if (!user.getEmail().equals(existingUser.getEmail())) {
            // Check if email already exists for other user (exclude current user)
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

        // Preserve other important fields
        // Only preserve password if not explicitly set in the user object
        if (user.getPassword() == null) {
            user.setPassword(existingUser.getPassword());
        }
        user.setRole(existingUser.getRole());
        user.setStatus(existingUser.getStatus());
        user.setId(existingUser.getId());

        // Update user in database
        return userDAO.updateUser(user);
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // Get user by username
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Verify old password
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Hash new password and update
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        // Directly update user without calling updateUser method to avoid password override
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
        // Check if username already exists
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if phone already exists
        if (user.getPhone() != null && !user.getPhone().isEmpty() && existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // Check if email already exists
        if (user.getEmail() != null && !user.getEmail().isEmpty() && existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Hash password
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));

        // Set default status
        user.setStatus(1);

        // Add user to database
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