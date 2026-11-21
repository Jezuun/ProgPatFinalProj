package com.conference.validation;

import com.conference.dao.UserDao;
import com.conference.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Validate {
    private final UserDao userDao = new UserDao();

    public User login(String email, String password) {
        User user = userDao.findByEmail(email);
        if (user == null) return null;
        String stored = userDao.getPasswordHash(user.getUserId());
        if (stored != null && stored.equals(hash(password))) {
            return user;//get user if everything matches
        }
        return null;
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}