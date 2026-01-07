package com.smartcommunity.util;

public class GeneratePassword {
    public static void main(String[] args) {
        String password = "123456";
        String hashedPassword = PasswordUtil.hashPassword(password);
        System.out.println("Original password: " + password);
        System.out.println("Hashed password: " + hashedPassword);
    }
}