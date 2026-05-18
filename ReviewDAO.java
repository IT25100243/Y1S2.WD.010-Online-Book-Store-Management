package com.pageturner.dao;

import com.pageturner.model.Review;

import java.io.IOException;
import java.util.*;

/**
 * ABSTRACTION: Hides all review file I/O from server code.
 */
public class ReviewDAO {

    private final FileDatabase db;

    public ReviewDAO(FileDatabase db) {
        this.db = db;
    }

    public List<Review> findAll() throws IOException {
        List<String> lines = db.readLines(db.reviewsFile());
        List<Review> reviews = new ArrayList<>();
        for (String line : lines) {
            Review r = parseReview(line);
            if (r != null) reviews.add(r);
        }
        return reviews;
    }

    public List<Review> findByCategory(String category) throws IOException {
        List<Review> result = new ArrayList<>();
        for (Review r : findAll()) {
            if (r.getCategory().equalsIgnoreCase(category)) result.add(r);
        }
        return result;
    }

    public void save(Review review) throws IOException {
        db.appendLine(db.reviewsFile(), review.toJson());
    }

    private Review parseReview(String json) {
        try {
            String id        = BookDAO.extract(json, "id");
            String username  = BookDAO.extract(json, "username");
            String category  = BookDAO.extract(json, "category");
            String bookTitle = BookDAO.extract(json, "bookTitle");
            String text      = BookDAO.extract(json, "text");
            int rating = Integer.parseInt(BookDAO.extractNum(json, "rating"));
            return new Review(id, username, category, bookTitle, rating, text);
        } catch (Exception e) {
            System.err.println("Failed to parse review: " + e.getMessage());
            return null;
        }
    }
}
