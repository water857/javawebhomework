package com.smartcommunity.util;

public class PasswordUtilTest {
    public static void main(String[] args) {
        // Test password verification for user 'lihao'
        String storedPassword = "5GiIGJTHGiJmOpCV:82fa81ab4b53e9aae7905923f45f090effea2a35eb11891abb1b6977f9c01fd1";
        String inputPassword = "123456";
        
        System.out.println("Testing password verification for user 'lihao':");
        System.out.println("Stored password: " + storedPassword);
        System.out.println("Input password: " + inputPassword);
        
        // Verify password
        boolean isMatch = PasswordUtil.verifyPassword(inputPassword, storedPassword);
        System.out.println("Password match: " + isMatch);
        
        // Test generating new password hash
        String newHash = PasswordUtil.hashPassword(inputPassword);
        System.out.println("New hash for '123456': " + newHash);
        
        // Verify new hash
        boolean newMatch = PasswordUtil.verifyPassword(inputPassword, newHash);
        System.out.println("New hash match: " + newMatch);
    }
}