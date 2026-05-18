package com.pageturner.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * INHERITANCE: AdminUser extends Person extends BaseEntity.
 * ENCAPSULATION: permissions list is private — only hasPermission() exposes it.
 * POLYMORPHISM: getRole() → "Admin"; toJson() adds permissions field.
 */
public class AdminUser extends Person {

    // ENCAPSULATION: private permissions — returned as unmodifiable copy
    private final List<String> permissions;

    public AdminUser(String username, String password, String name, String email) {
        super("ADM-" + username, username, password, name, email);
        this.permissions = Arrays.asList(
            "MANAGE_BOOKS", "MANAGE_USERS", "VIEW_ORDERS",
            "MANAGE_STOCK", "VIEW_FEEDBACK"
        );
    }

    // For loading from file
    public AdminUser(String id, String username, String password,
                     String name, String email) {
        super(id, username, password, name, email);
        this.permissions = Arrays.asList(
            "MANAGE_BOOKS", "MANAGE_USERS", "VIEW_ORDERS",
            "MANAGE_STOCK", "VIEW_FEEDBACK"
        );
    }

    // POLYMORPHISM: Admin's role
    @Override
    public String getRole() { return "Admin"; }

    // ENCAPSULATION: returns unmodifiable copy — caller cannot mutate internal list
    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    // POLYMORPHISM: Admin-specific JSON includes permissions
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
        return "[Admin username=" + getUsername() +
               " permissions=" + permissions.size() + "]";
    }
}
