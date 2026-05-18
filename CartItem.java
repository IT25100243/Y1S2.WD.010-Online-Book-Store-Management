package com.pageturner.model;

/**
 * INHERITANCE: CartItem extends BaseEntity.
 * ENCAPSULATION: quantity is private with validated setter.
 * Represents one line item in a user's shopping cart.
 * Relationship: Many CartItems belong to one User (via username).
 */
public class CartItem extends BaseEntity {

    private final String bookId;
    private final String username;  // owner relationship

    // ENCAPSULATION: quantity is private — validated on every change
    private int quantity;

    public CartItem(String bookId, String username, int quantity) {
        super("CI-" + bookId + "-" + username + "-" + System.currentTimeMillis());
        this.bookId   = bookId;
        this.username = username;
        setQuantity(quantity);
    }

    public String getBookId()   { return bookId; }
    public String getUsername() { return username; }
    public int    getQuantity() { return quantity; }

    // ENCAPSULATION: validated quantity setter
    public void setQuantity(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1.");
        this.quantity = quantity;
    }

    public void increment() { quantity++; }

    public void decrement() {
        if (quantity > 1) quantity--;
    }

    public double lineTotal(double unitPrice) {
        return unitPrice * quantity;
    }

    @Override
    public String getEntityType() { return "CartItem"; }

    @Override
    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"bookId\":\"%s\",\"username\":\"%s\",\"quantity\":%d}",
            getId(), bookId, username, quantity
        );
    }

    @Override
    public String toString() {
        return "[CartItem bookId=" + bookId + " user=" + username +
               " qty=" + quantity + "]";
    }
}
