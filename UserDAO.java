package com.pageturner.dao;

import com.pageturner.model.AdminUser;
import com.pageturner.model.Person;
import com.pageturner.model.User;

import java.io.IOException;
import java.util.*;

/**
 * ABSTRACTION: Hides all user file-I/O from server code.
 * POLYMORPHISM: Returns Person references — could be User or AdminUser.
 */
public class UserDAO {

    private final FileDatabase db;

    public UserDAO(FileDatabase db) {
        this.db = db;
    }

    // ── READ ──────────────────────────────────────────────────────────────

    public List<User> findAllUsers() throws IOException {
        List<String> lines = db.readLines(db.usersFile());
        List<User> users = new ArrayList<>();
        for (String line : lines) {
            Person p = parsePerson(line);
            if (p instanceof User u) users.add(u);
        }
        return users;
    }

    public Optional<Person> findByUsername(String username) throws IOException {
        for (String line : db.readLines(db.usersFile())) {
            Person p = parsePerson(line);
            if (p != null && p.getUsername().equals(username)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public boolean usernameExists(String username) throws IOException {
        return findByUsername(username).isPresent();
    }

    // ── WRITE ─────────────────────────────────────────────────────────────

    public void save(Person person) throws IOException {
        db.appendLine(db.usersFile(), person.toJson());
    }

    public void delete(String username) throws IOException {
        Optional<Person> p = findByUsername(username);
        if (p.isEmpty()) return;
        db.deleteById(db.usersFile(), p.get().getId());
        db.deleteCartFile(username);
    }

    // ── JSON PARSING ─────────────────────────────────────────────────────

    private Person parsePerson(String json) {
        try {
            String id       = BookDAO.extract(json, "id");
            String username = BookDAO.extract(json, "username");
            String password = BookDAO.extract(json, "password");
            String name     = BookDAO.extract(json, "name");
            String email    = BookDAO.extract(json, "email");
            String role     = BookDAO.extract(json, "role");

            // POLYMORPHISM: factory — returns correct subclass based on role
            if ("Admin".equals(role)) {
                return new AdminUser(id, username, password, name, email);
            }
            return new User(id, username, password, name, email);
        } catch (Exception e) {
            System.err.println("Failed to parse person JSON: " + e.getMessage());
            return null;
        }
    }
}
