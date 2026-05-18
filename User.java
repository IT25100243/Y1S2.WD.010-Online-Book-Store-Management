package com.pageturner.model;

/**
 * INHERITANCE: User extends Person extends BaseEntity (3-level chain).
 * Represents a registered customer.
 * POLYMORPHISM: getRole() returns "Customer"; toJson() produces user-specific JSON.
 */
public class User extends Person {

    public User(String username, String password, String name, String email) {
        // INHERITANCE: calls Person constructor which calls BaseEntity constructor
        super("USR-" + username, username, password, name, email);
    }

    // Constructor for loading from file (with existing id and createdAt)
    public User(String id, String username, String password,
                String name, String email) {
        super(id, username, password, name, email);
    }

    // POLYMORPHISM: User's role
    @Override
    public String getRole() { return "Customer"; }

    // POLYMORPHISM: User-specific JSON serialisation
    @Override
    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"," +
            "\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"createdAt\":\"%s\"}",
            getId(), getUsername(), getRawPassword(),
            getName(), getEmail(), getRole(), getCreatedAt()
        );
    }

    @Override
    public String toString() {
        return "[Customer username=" + getUsername() + " name=" + getName() + "]";
    }
}
