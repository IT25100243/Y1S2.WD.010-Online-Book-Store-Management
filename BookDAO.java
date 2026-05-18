package com.pageturner.dao;

import com.pageturner.model.Book;
import com.pageturner.model.DigitalBook;
import com.pageturner.model.BookItem;

import java.io.IOException;
import java.util.*;

/**
 * ABSTRACTION: BookDAO hides all file I/O details from the rest of the app.
 * The server servlets never touch files directly — they only call DAO methods.
 *
 * Parses JSON manually (no external parser needed for our simple format).
 */
public class BookDAO {

    private final FileDatabase db;

    public BookDAO(FileDatabase db) {
        this.db = db;
    }

    // ── READ ──────────────────────────────────────────────────────────────

    public List<Book> findAll() throws IOException {
        List<String> lines = db.readLines(db.booksFile());
        List<Book> books = new ArrayList<>();
        for (String line : lines) {
            Book b = parseBook(line);
            if (b != null) books.add(b);
        }
        return books;
    }

    public List<Book> findByCategory(String category) throws IOException {
        List<Book> result = new ArrayList<>();
        for (Book b : findAll()) {
            if (b.getCategory().equalsIgnoreCase(category)) result.add(b);
        }
        return result;
    }

    public Optional<Book> findById(String id) throws IOException {
        for (Book b : findAll()) {
            if (b.getId().equals(id)) return Optional.of(b);
        }
        return Optional.empty();
    }

    // ── WRITE ─────────────────────────────────────────────────────────────

    public void save(Book book) throws IOException {
        db.appendLine(db.booksFile(), book.toJson());
    }

    public void updateStock(String bookId, int newStock) throws IOException {
        Optional<Book> opt = findById(bookId);
        if (opt.isEmpty()) return;
        Book b = opt.get();
        b.setStock(newStock);
        db.updateById(db.booksFile(), bookId, b.toJson());
    }

    public void delete(String bookId) throws IOException {
        db.deleteById(db.booksFile(), bookId);
    }

    // ── JSON PARSING (manual — demonstrates OOP factory pattern) ──────────

    private Book parseBook(String json) {
        try {
            String id     = extract(json, "id");
            String cat    = extract(json, "cat");
            String title  = extract(json, "title");
            String author = extract(json, "author");
            double price  = Double.parseDouble(extractNum(json, "price"));
            int    pages  = Integer.parseInt(extractNum(json, "pages"));
            int    stock  = Integer.parseInt(extractNum(json, "stock"));
            String img    = extract(json, "img");
            String desc   = extract(json, "desc");
            String type   = extract(json, "type");

            if ("digital".equals(type)) {
                String fmt = extract(json, "fileFormat");
                return new DigitalBook(id, cat, title, author, price, pages,
                                       img, desc, fmt != null ? fmt : "PDF", "");
            }
            return new Book(id, cat, title, author, price, pages, stock, img, desc);
        } catch (Exception e) {
            System.err.println("Failed to parse book JSON: " + e.getMessage());
            return null;
        }
    }

    // ── Simple JSON field extractors ──────────────────────────────────────

    static String extract(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end)
                   .replace("\\\"", "\"")
                   .replace("\\n", "\n")
                   .replace("\\\\", "\\");
    }

    static String extractNum(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "0";
        start += search.length();
        // skip quote if present (string number)
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        int end = start;
        while (end < json.length() &&
               (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
            end++;
        }
        String val = json.substring(start, end);
        return val.isEmpty() ? "0" : val;
    }
}
