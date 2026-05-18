package com.pageturner.dao;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FILE-BASED DATABASE
 * Simulates a database using plain JSON text files on disk.
 * Each entity type has its own file (e.g. books.json, users.json).
 *
 * ENCAPSULATION: dataDir is private; all I/O goes through read/write methods.
 * ABSTRACTION:   Higher-level DAOs use readLines() / writeLine() / deleteById()
 *                without knowing the file path details.
 */
public class FileDatabase {

    // ENCAPSULATION: private — no outside code touches the path directly
    private final Path dataDir;

    private static final String BOOKS_FILE   = "books.json";
    private static final String USERS_FILE   = "users.json";
    private static final String REVIEWS_FILE = "reviews.json";
    private static final String ORDERS_FILE  = "orders.json";
    private static final String CARTS_DIR    = "carts";       // one file per user

    public FileDatabase(String dataDirPath) throws IOException {
        this.dataDir = Paths.get(dataDirPath);
        // Create directories if they don't exist
        Files.createDirectories(dataDir);
        Files.createDirectories(dataDir.resolve(CARTS_DIR));

        // Seed files if empty
        ensureFile(BOOKS_FILE);
        ensureFile(USERS_FILE);
        ensureFile(REVIEWS_FILE);
        ensureFile(ORDERS_FILE);
    }

    // ── Generic file operations ────────────────────────────────────────────

    /** Read all non-empty lines from a file (each line is one JSON record) */
    public List<String> readLines(String filename) throws IOException {
        Path file = dataDir.resolve(filename);
        if (!Files.exists(file)) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (String line : Files.readAllLines(file)) {
            if (!line.isBlank()) result.add(line.trim());
        }
        return result;
    }

    /** Append a new JSON line to a file */
    public void appendLine(String filename, String json) throws IOException {
        Path file = dataDir.resolve(filename);
        Files.writeString(file, json + "\n", StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }

    /** Rewrite entire file with updated list of JSON lines */
    public void writeAllLines(String filename, List<String> jsonLines) throws IOException {
        Path file = dataDir.resolve(filename);
        StringBuilder sb = new StringBuilder();
        for (String line : jsonLines) {
            sb.append(line).append("\n");
        }
        Files.writeString(file, sb.toString(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    /** Delete a record by id from a file (removes the line containing "id":"<id>") */
    public void deleteById(String filename, String id) throws IOException {
        List<String> lines  = readLines(filename);
        List<String> kept   = new ArrayList<>();
        String target = "\"id\":\"" + id + "\"";
        for (String line : lines) {
            if (!line.contains(target)) kept.add(line);
        }
        writeAllLines(filename, kept);
    }

    /** Update a record: replace the line whose id matches with newJson */
    public void updateById(String filename, String id, String newJson) throws IOException {
        List<String> lines  = readLines(filename);
        List<String> result = new ArrayList<>();
        String target = "\"id\":\"" + id + "\"";
        for (String line : lines) {
            result.add(line.contains(target) ? newJson : line);
        }
        writeAllLines(filename, result);
    }

    // ── Convenience filename getters ───────────────────────────────────────

    public String booksFile()   { return BOOKS_FILE; }
    public String usersFile()   { return USERS_FILE; }
    public String reviewsFile() { return REVIEWS_FILE; }
    public String ordersFile()  { return ORDERS_FILE; }

    /** Each user has their own cart file: carts/<username>.json */
    public String cartFile(String username) {
        return CARTS_DIR + File.separator + username + ".json";
    }

    public void deleteCartFile(String username) throws IOException {
        Path cartPath = dataDir.resolve(CARTS_DIR).resolve(username + ".json");
        Files.deleteIfExists(cartPath);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void ensureFile(String filename) throws IOException {
        Path file = dataDir.resolve(filename);
        if (!Files.exists(file)) Files.createFile(file);
    }

    public Path getDataDir() { return dataDir; }
}
