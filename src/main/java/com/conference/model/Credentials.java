package com.conference.model;

/** Login details, stored separately from the user profile. */
public record Credentials(int userId, String passwordHash, String salt) {
}
