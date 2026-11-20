package com.conference.model;

public class User {
    private int userId;
    private String fullName;
    private String email;
    private int age;
    private Role role;

    public User(int userId, String fullName, String email, int age, Role role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}