package com.smartcommunity.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordTest {
    public static void main(String[] args) {
        // 测试已知的盐值和密码
        String salt = "5GiIGJTHGiJmOpCV";
        String password = "123456";
        String storedHash = "82fa81ab4b53e9aae7905923f45f090effea2a35eb11891abb1b6977f9c01fd1";
        
        System.out.println("Testing password verification...");
        System.out.println("Salt: " + salt);
        System.out.println("Password: " + password);
        System.out.println("Stored hash: " + storedHash);
        
        // 测试不同编码
        testEncoding(salt, password, storedHash, "UTF-8");
        testEncoding(salt, password, storedHash, "UTF-16");
        testEncoding(salt, password, storedHash, "UTF-16BE");
        testEncoding(salt, password, storedHash, "UTF-16LE");
        testEncoding(salt, password, storedHash, "GBK");
        testEncoding(salt, password, storedHash, "ISO-8859-1");
        
        // 测试PasswordUtil的verifyPassword方法
        String storedPassword = salt + ":" + storedHash;
        boolean result = PasswordUtil.verifyPassword(password, storedPassword);
        System.out.println("\nPasswordUtil.verifyPassword result: " + result);
        
        // 测试PasswordUtil的hashPassword方法
        String hashed = PasswordUtil.hashPassword(password);
        System.out.println("\nNewly hashed password: " + hashed);
        String[] parts = hashed.split(":");
        if (parts.length == 2) {
            System.out.println("New salt: " + parts[0]);
            System.out.println("New hash: " + parts[1]);
            // 验证新生成的密码
            boolean newResult = PasswordUtil.verifyPassword(password, hashed);
            System.out.println("Verification of new hash: " + newResult);
        }
    }
    
    private static void testEncoding(String salt, String password, String storedHash, String encoding) {
        try {
            String saltedPassword = salt + password;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(saltedPassword.getBytes(encoding));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String generatedHash = sb.toString();
            System.out.println("\nUsing " + encoding + ":");
            System.out.println("Generated hash: " + generatedHash);
            System.out.println("Match: " + generatedHash.equals(storedHash));
        } catch (Exception e) {
            System.out.println("Error with " + encoding + ": " + e.getMessage());
        }
    }
}