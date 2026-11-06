package com.conference.model;

/** Profile data of a person using the application (no password here). */
public class User {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private int age;
    private Role role;

    public User(String firstName, String lastName, String email, int age, Role role) {
        this(0, firstName, lastName, email, age, role);
    }

    public User(int id, String firstName, String lastName, String email, int age, Role role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + getFullName() + ", email=" + email + ", age=" + age
                + ", role=" + role + "}";
    }
}
