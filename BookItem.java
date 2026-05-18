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

    /**
    * Creates a purchasable catalogue item.
    *
    * Initializes common properties shared by all
    * item types such as title, author, category,
    * and validated price.
    *
    * @param id unique item identifier
    * @param title book title
    * @param author author name
    * @param price item price
    * @param category book category
    */

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

    /**
    * Updates item price after validation.
    *
    * Business rule:
    * Price cannot be negative.
    *
    * @param price new item price
    * @throws IllegalArgumentException
    * if price is below zero
    */

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }
    /**
    * Converts numeric price to display format.
    *
    * Example:
    * 1500 → LKR 1500
    *
    * @return formatted currency string
    */

    public String getFormattedPrice() {
        return String.format("LKR %.0f", price);
    }

    /**
    * Returns stock status information.
    *
    * Implemented differently by subclasses.
    */
    public abstract String getStockStatus();
    
    /**
    * Returns CSS class used by frontend
    * to visually represent stock level.
    */
    public abstract String getStockClass();
    
    /**
    * Determines whether the item
    * is currently available for purchase.
    */
    public abstract boolean isAvailable();
    
    /**
    * Returns the specific item type.
    *
    * Examples:
    * - Book
    * - DigitalBook
    */
    public abstract String getItemType();

    @Override
    public String toString() {
        return "[" + getItemType() + " \"" + title + "\" by " + author +
               " " + getFormattedPrice() + "]";
    }
}
