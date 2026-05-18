package com.pageturner.model;

/**
 * ABSTRACTION: Abstract base for all purchasable catalogue items.
 * ENCAPSULATION: price is private with validated setter.
 * INHERITANCE: Extends BaseEntity.
 *
 * Subclasses: Book, DigitalBook
 */
public abstract class BookItem extends BaseEntity {

    // ENCAPSULATION: price is private — validated on every write
    private double price;
    private final String title;
    private final String author;
    private final String category;

    protected BookItem(String id, String title, String author,
                       double price, String category) {
        super(id);
        this.title    = title;
        this.author   = author;
        this.category = category;
        setPrice(price);  // use setter for validation at construction
    }

    // ENCAPSULATION: getter
    public String getTitle()    { return title; }
    public String getAuthor()   { return author; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }

    // ENCAPSULATION: validated setter — negative price is rejected
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }

    public String getFormattedPrice() {
        return String.format("LKR %.0f", price);
    }

    // ABSTRACTION: subclasses define their stock availability
    public abstract String getStockStatus();
    public abstract String getStockClass();
    public abstract boolean isAvailable();

    // ABSTRACTION: subclasses define their type label
    public abstract String getItemType();

    // POLYMORPHISM: toString differs per subclass
    @Override
    public String toString() {
        return "[" + getItemType() + " \"" + title + "\" by " + author +
               " " + getFormattedPrice() + "]";
    }
}
