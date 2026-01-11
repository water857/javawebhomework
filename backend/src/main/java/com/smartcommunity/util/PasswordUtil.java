package com.smartcommunity.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    // 生成盐值（仅用于旧版 SHA-256 格式）
    private static String generateSalt() {
        return RandomStringUtils.randomAlphanumeric(SALT_LENGTH);
    }

    /**
     * ✅ 新注册用户：统一用 BCrypt（推荐）
     */
    public static String hashPassword(String password) {
        return BCRYPT.encode(password);

        // 如果你非要继续用 salt:sha256，就改成下面这三行：
        // String salt = generateSalt();
        // String hashedPassword = hash(salt + password);
        // return salt + ":" + hashedPassword;
    }

    /**
     * ✅ 登录校验：兼容两种格式
     * 1) BCrypt: $2a$ / $2b$ / $2y$
     * 2) Legacy SHA-256: salt:hash
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        if (storedPassword == null || storedPassword.isEmpty()) {return false;}

        // 使用 BCrypt
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return BCRYPT.matches(password, storedPassword);
        }

        // 旧版 salt:sha256
        if (storedPassword.contains(":")) {
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {return false;}
            String salt = parts[0];
            String hashedPassword = parts[1];
            return hashedPassword.equals(hash(salt + password));
        }

        return false;
    }

    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {sb.append(String.format("%02x", b));}
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
