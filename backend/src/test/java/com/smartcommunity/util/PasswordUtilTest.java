package com.smartcommunity.util;

public class PasswordUtilTest {
    public static void main(String[] args) {
        // 测试用户“lihao”的密码校验
        String storedPassword = "5GiIGJTHGiJmOpCV:82fa81ab4b53e9aae7905923f45f090effea2a35eb11891abb1b6977f9c01fd1";
        String inputPassword = "123456";
        
        System.out.println("Testing password verification for user 'lihao':");
        System.out.println("Stored password: " + storedPassword);
        System.out.println("Input password: " + inputPassword);
        
        // 校验密码
        boolean isMatch = PasswordUtil.verifyPassword(inputPassword, storedPassword);
        System.out.println("Password match: " + isMatch);
        
        // 测试生成新的密码哈希
        String newHash = PasswordUtil.hashPassword(inputPassword);
        System.out.println("New hash for '123456': " + newHash);
        
        // 校验新的哈希
        boolean newMatch = PasswordUtil.verifyPassword(inputPassword, newHash);
        System.out.println("New hash match: " + newMatch);
    }
}
