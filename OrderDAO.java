package com.pageturner.dao;

import com.pageturner.model.Order;
import com.pageturner.model.Order.CartItemSnapshot;

import java.io.IOException;
import java.util.*;

/**
 * ABSTRACTION: Hides all order file I/O from server code.
 */
public class OrderDAO {

    private final FileDatabase db;

    public OrderDAO(FileDatabase db) {
        this.db = db;
    }

    public List<Order> findAll() throws IOException {
        List<String> lines = db.readLines(db.ordersFile());
        List<Order> orders = new ArrayList<>();
        for (String line : lines) {
            Order o = parseOrder(line);
            if (o != null) orders.add(o);
        }
        return orders;
    }

    public List<Order> findByUsername(String username) throws IOException {
        List<Order> result = new ArrayList<>();
        for (Order o : findAll()) {
            if (o.getUsername().equals(username)) result.add(o);
        }
        return result;
    }

    public void save(Order order) throws IOException {
        db.appendLine(db.ordersFile(), order.toJson());
    }

    // ── JSON Parsing ─────────────────────────────────────────────────────

    private Order parseOrder(String json) {
        try {
            String id       = BookDAO.extract(json, "id");
            String username = BookDAO.extract(json, "username");
            double total    = Double.parseDouble(BookDAO.extractNum(json, "total"));
            double shipping = Double.parseDouble(BookDAO.extractNum(json, "shippingCost"));
            String status   = BookDAO.extract(json, "status");
            if (status == null) status = "Placed";

            // Parse items array (simplified)
            List<CartItemSnapshot> items = parseSnapshots(json);
            return new Order(id, username, items, total, shipping, status);
        } catch (Exception e) {
            System.err.println("Failed to parse order: " + e.getMessage());
            return null;
        }
    }

    private List<CartItemSnapshot> parseSnapshots(String json) {
        List<CartItemSnapshot> list = new ArrayList<>();
        int itemsStart = json.indexOf("\"items\":[");
        if (itemsStart == -1) return list;
        itemsStart += 9;
        int depth = 1;
        int i = itemsStart;
        int objStart = -1;
        while (i < json.length() && depth > 0) {
            char c = json.charAt(i);
            if (c == '{') { if (depth == 1) objStart = i; depth++; }
            else if (c == '}') {
                depth--;
                if (depth == 1 && objStart != -1) {
                    String obj = json.substring(objStart, i + 1);
                    String bookId    = BookDAO.extract(obj, "bookId");
                    String bookTitle = BookDAO.extract(obj, "bookTitle");
                    double unitPrice = Double.parseDouble(BookDAO.extractNum(obj, "unitPrice"));
                    int qty = Integer.parseInt(BookDAO.extractNum(obj, "quantity"));
                    if (bookId != null) {
                        list.add(new CartItemSnapshot(bookId,
                                bookTitle != null ? bookTitle : "", unitPrice, qty));
                    }
                    objStart = -1;
                }
            } else if (c == ']' && depth == 1) break;
            i++;
        }
        return list;
    }
}
