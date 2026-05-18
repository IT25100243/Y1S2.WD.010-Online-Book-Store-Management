package com.pageturner.model;

/**
 * INHERITANCE: Book extends BookItem extends BaseEntity (3-level chain).
 * ENCAPSULATION: stock is private with validated setter.
 * POLYMORPHISM: getStockStatus(), getItemType(), toJson() all specific to Book.
 */
public class Book extends BookItem {

    // ENCAPSULATION: stock is private — validated on every write
    private int    stock;
    private final int    pages;
    private final String imageUrl;
    private final String description;

    public Book(String id, String category, String title, String author,
                double price, int pages, int stock, String imageUrl, String description) {
        super(id, title, author, price, category);
        this.pages       = pages;
        this.imageUrl    = imageUrl;
        this.description = description;
        setStock(stock);  // validated via setter
    }

    // ENCAPSULATION: validated stock setter
    public int getStock() { return stock; }

    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.stock = stock;
    }

    public void addStock(int qty) {
        if (qty < 0) throw new IllegalArgumentException("Cannot add negative stock.");
        this.stock += qty;
    }

    public boolean decrementStock() {
        if (stock <= 0) return false;
        stock--;
        return true;
    }

    public int    getPages()       { return pages; }
    public String getImageUrl()    { return imageUrl; }
    public String getDescription() { return description; }

    // POLYMORPHISM: Book-specific stock status
    @Override
    public String getStockStatus() {
        if (stock > 20) return "In Stock";
        if (stock > 0)  return "Low Stock";
        return "Out of Stock";
    }

    @Override
    public String getStockClass() {
        if (stock > 20) return "stock-ok";
        if (stock > 0)  return "stock-low";
        return "stock-out";
    }

    @Override
    public boolean isAvailable() { return stock > 0; }

    // POLYMORPHISM: Book's type label
    @Override
    public String getItemType() { return "Book"; }

    // POLYMORPHISM: Book-specific entity type
    @Override
    public String getEntityType() { return "Book"; }

    // POLYMORPHISM: Book-specific JSON serialisation
    @Override
    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"cat\":\"%s\",\"title\":\"%s\",\"author\":\"%s\"," +
            "\"price\":%.0f,\"pages\":%d,\"stock\":%d,\"img\":\"%s\"," +
            "\"desc\":\"%s\",\"type\":\"physical\"}",
            getId(), getCategory(), escape(getTitle()), escape(getAuthor()),
            getPrice(), pages, stock, imageUrl, escape(description)
        );
    }

    // Escape quotes for JSON safety
    protected String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    @Override
    public String toString() {
        return "[Book \"" + getTitle() + "\" by " + getAuthor() +
               " cat=" + getCategory() + " " + getFormattedPrice() +
               " stock=" + stock + "]";
    }
}
