package com.pageturner.model;

/**
 * INHERITANCE: Review extends BaseEntity.
 * ENCAPSULATION: rating is private with validation (1–5 only).
 * Relationship: One Review belongs to one User, references a book category.
 */
public class Review extends BaseEntity {

    private final String username;
    private final String category;
    private final String bookTitle;

    // ENCAPSULATION: rating is private — validated in constructor and setter
    private int    rating;
    private final String text;

    public Review(String username, String category,
                  String bookTitle, int rating, String text) {
        super("REV-" + System.currentTimeMillis() + "-" +
              Math.abs(username.hashCode() % 1000));
        this.username  = username;
        this.category  = category;
        this.bookTitle = bookTitle;
        this.text      = text;
        setRating(rating);
    }

    // For loading from file
    public Review(String id, String username, String category,
                  String bookTitle, int rating, String text) {
        super(id);
        this.username  = username;
        this.category  = category;
        this.bookTitle = bookTitle;
        this.text      = text;
        setRating(rating);
    }

    public String getUsername()  { return username; }
    public String getCategory()  { return category; }
    public String getBookTitle() { return bookTitle; }
    public int    getRating()    { return rating; }
    public String getText()      { return text; }

    // ENCAPSULATION: validated rating setter
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.rating = rating;
    }

    // POLYMORPHISM: visual star string — computed from private rating
    public String getStars() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    @Override
    public String getEntityType() { return "Review"; }

    @Override
    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"username\":\"%s\",\"category\":\"%s\"," +
            "\"bookTitle\":\"%s\",\"rating\":%d,\"text\":\"%s\",\"createdAt\":\"%s\"}",
            getId(), username, category,
            bookTitle.replace("\"","\\\""),
            rating, text.replace("\"","\\\""), getCreatedAt()
        );
    }

    @Override
    public String toString() {
        return "[Review user=" + username + " book=\"" + bookTitle +
               "\" " + getStars() + "]";
    }
}
