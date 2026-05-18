package com.pageturner.model;

/**
 * ABSTRACTION: Abstract base class for all people in the system.
 * INHERITANCE: Extends BaseEntity.
 * ENCAPSULATION: password is private — only verifyPassword() exposes it indirectly.
 *                email is private with a getter only.
 *
 * Subclasses: User, AdminUser
 */

public abstract class Person extends BaseEntity {

    // ENCAPSULATION: private fields — cannot be accessed directly from outside
    private final String username;
    private String       password;   // write-allowed via changePassword()
    private final String name;
    private final String email;

    protected Person(String id, String username, String password,
                     String name, String email) {
        super(id);
        this.username = username;
        this.password = password;
        this.name     = name;
        this.email    = email;
    }

    // ENCAPSULATION: read-only getters for most fields
    public String getUsername() { return username; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }

    // ENCAPSULATION: password is never returned directly
    public boolean verifyPassword(String attempt) {
        return this.password.equals(attempt);
    }

    // ENCAPSULATION: validated password change
    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        this.password = newPassword;
    }

    // For file persistence only — package-private 
    String getRawPassword() { return password; }

    // ABSTRACTION: subclasses must declare their role
    public abstract String getRole();

    // POLYMORPHISM: toString uses getRole() so output differs per subclass
    @Override
    public String toString() {
        return "[" + getRole() + " username=" + username + " name=" + name + "]";
    }

    @Override
    public String getEntityType() { return getRole(); }
}
