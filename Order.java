package com.pageturner.model;

import java.util.Collections;
import java.util.List;

/**
 * INHERITANCE: Order extends BaseEntity.
 * ENCAPSULATION: total is private and final — read-only after construction.
 *                items list is private — returned as unmodifiable copy.
 * Relationship: One Order belongs to one User; contains many CartItem snapshots.
 */
public class Order extends BaseEntity {

    private final String username;

    // ENCAPSULATION: private & final — total can never change after placing
    private final double total;
    private final double shippingCost;

    // ENCAPSULATION: items list is private — defensive copy returned
    private final List<CartItemSnapshot> items;
    private String status;

    public Order(String username, List<CartItemSnapshot> items,
                 double subtotal, double shippingCost) {
        super("ORD-" + System.currentTimeMillis());
        this.username     = username;
        this.shippingCost = shippingCost;
        this.total        = subtotal + shippingCost;
        this.items        = List.copyOf(items);  // ENCAPSULATION: immutable copy
        this.status       = "Placed";
    }

    // For loading from file
    public Order(String id, String username, List<CartItemSnapshot> items,
                 double total, double shippingCost, String status) {
        super(id);
        this.username     = username;
        this.items        = List.copyOf(items);
        this.total        = total;
        this.shippingCost = shippingCost;
        this.status       = status;
    }

    public String getUsername()     { return username; }
    public double getTotal()        { return total; }
    public double getShippingCost() { return shippingCost; }
    public String getStatus()       { return status; }

    // ENCAPSULATION: status can be updated but only to valid values
    public void setStatus(String status) {
        if (!List.of("Placed","Processing","Shipped","Delivered","Cancelled").contains(status)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        this.status = status;
    }

    // ENCAPSULATION: returns unmodifiable view — caller cannot add/remove items
    public List<CartItemSnapshot> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getFormattedTotal() {
        return String.format("LKR %.0f", total);
    }

    @Override
    public String getEntityType() { return "Order"; }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
            "{\"id\":\"%s\",\"username\":\"%s\",\"total\":%.0f," +
            "\"shippingCost\":%.0f,\"status\":\"%s\",\"createdAt\":\"%s\",\"items\":[",
            getId(), username, total, shippingCost, status, getCreatedAt()
        ));
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toJson());
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "[Order id=" + getId() + " user=" + username +
               " total=" + getFormattedTotal() + " status=" + status + "]";
    }

    // ── Inner class: snapshot of a cart item at order time ─────────────────
    /**
     * ENCAPSULATION: Snapshot stores bookId, title, price, qty at the time of
     * ordering — price changes later don't affect historical orders.
     */
    public static class CartItemSnapshot {
        private final String bookId;
        private final String bookTitle;
        private final double unitPrice;
        private final int    quantity;

        public CartItemSnapshot(String bookId, String bookTitle,
                                double unitPrice, int quantity) {
            this.bookId    = bookId;
            this.bookTitle = bookTitle;
            this.unitPrice = unitPrice;
            this.quantity  = quantity;
        }

        public String getBookId()    { return bookId; }
        public String getBookTitle() { return bookTitle; }
        public double getUnitPrice() { return unitPrice; }
        public int    getQuantity()  { return quantity; }
        public double getLineTotal() { return unitPrice * quantity; }

        public String toJson() {
            return String.format(
                "{\"bookId\":\"%s\",\"bookTitle\":\"%s\",\"unitPrice\":%.0f,\"quantity\":%d}",
                bookId, bookTitle.replace("\"","\\\""), unitPrice, quantity
            );
        }
    }
}
