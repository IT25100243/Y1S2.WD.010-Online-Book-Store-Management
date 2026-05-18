package com.bookstore.server;

import com.pageturner.dao.*;
import com.pageturner.model.*;
import com.pageturner.model.BaseEntity;
import com.pageturner.model.Order.CartItemSnapshot;
import com.pageturner.util.AppContext;

import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.util.*;

/**
 * REST API servlet — handles all /api/* routes.
 * Every route logs to console so you can see what is happening in IntelliJ.
 */
public class ApiServlet extends HttpServlet {

    private AppContext ctx;

    @Override
    public void init() throws ServletException {
        try {
            System.out.println("[ApiServlet] Initialising...");
            ctx = AppContext.getInstance();
            System.out.println("[ApiServlet] Ready.");
        } catch (IOException e) {
            System.err.println("[ApiServlet] FAILED TO INIT: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Failed to initialise AppContext", e);
        }
    }

    // ── POST ──────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";
        setCors(resp);
        resp.setContentType("application/json; charset=UTF-8");
        String body = readBody(req);
        System.out.println("[POST] " + path + " body=" + body);

        try {
            switch (path) {
                case "/auth/login"    -> handleLogin(body, resp);
                case "/auth/register" -> handleRegister(body, resp);
                case "/books"         -> handleAddBook(body, resp);
                case "/cart"          -> handleAddToCart(body, resp);
                case "/orders"        -> handleCheckout(body, resp);
                case "/reviews"       -> handleAddReview(body, resp);
                default               -> error(resp, 404, "Unknown POST path: " + path);
            }
        } catch (Exception e) {
            System.err.println("[POST] ERROR: " + e.getMessage());
            e.printStackTrace();
            error(resp, 500, e.getMessage());
        }
    }

    // ── GET ───────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";
        setCors(resp);
        resp.setContentType("application/json; charset=UTF-8");
        System.out.println("[GET] " + path + " query=" + req.getQueryString());

        try {
            if (path.equals("/books")) {
                String cat = req.getParameter("cat");
                List<Book> books;
                if (cat != null && !cat.isBlank()) {
                    books = ctx.getBookDAO().findByCategory(cat.trim().toLowerCase());
                    System.out.println("[GET /books] cat=" + cat + " found=" + books.size());
                } else {
                    books = ctx.getBookDAO().findAll();
                    System.out.println("[GET /books] all, found=" + books.size());
                }
                json(resp, listToJson(books));

            } else if (path.matches("/books/[^/]+")) {
                String id = path.substring(7);
                Optional<Book> b = ctx.getBookDAO().findById(id);
                if (b.isPresent()) {
                    json(resp, b.get().toJson());
                } else {
                    System.err.println("[GET /books/" + id + "] NOT FOUND");
                    error(resp, 404, "Book not found: " + id);
                }

            } else if (path.equals("/cart")) {
                String user = req.getParameter("user");
                if (user == null || user.isBlank()) {
                    error(resp, 400, "user parameter required"); return;
                }
                List<CartItem> items = ctx.getCartDAO().getCart(user);
                json(resp, cartToJson(items));

            } else if (path.equals("/orders")) {
                String user = req.getParameter("user");
                if (user == null || user.isBlank()) {
                    error(resp, 400, "user parameter required"); return;
                }
                List<Order> orders = ctx.getOrderDAO().findByUsername(user);
                json(resp, listToJson(orders));

            } else if (path.equals("/orders/all")) {
                List<Order> orders = ctx.getOrderDAO().findAll();
                json(resp, listToJson(orders));

            } else if (path.equals("/reviews")) {
                String cat = req.getParameter("cat");
                List<Review> reviews = (cat != null && !cat.isBlank())
                        ? ctx.getReviewDAO().findByCategory(cat)
                        : ctx.getReviewDAO().findAll();
                json(resp, listToJson(reviews));

            } else if (path.equals("/users/all")) {
                List<User> users = ctx.getUserDAO().findAllUsers();
                json(resp, listToJson(users));

            } else if (path.equals("/stats")) {
                handleStats(resp);

            } else {
                System.err.println("[GET] Unknown path: " + path);
                error(resp, 404, "Unknown path: " + path);
            }

        } catch (Exception e) {
            System.err.println("[GET] ERROR on " + path + ": " + e.getMessage());
            e.printStackTrace();
            error(resp, 500, e.getMessage());
        }
    }

    // ── PUT ───────────────────────────────────────────────────────────────

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";
        setCors(resp);
        resp.setContentType("application/json; charset=UTF-8");
        String body = readBody(req);
        System.out.println("[PUT] " + path);

        try {
            if (path.matches("/books/[^/]+/stock")) {
                String[] parts = path.split("/");
                String id = parts[2];
                String stockStr = extractJson(body, "stock");
                if (stockStr == null) { error(resp, 400, "stock field required"); return; }
                int stock = Integer.parseInt(stockStr);
                ctx.getBookDAO().updateStock(id, stock);
                json(resp, "{\"success\":true}");

            } else if (path.equals("/cart")) {
                String user   = extractJson(body, "username");
                String bookId = extractJson(body, "bookId");
                String qtyStr = extractJson(body, "quantity");
                if (user == null || bookId == null || qtyStr == null) {
                    error(resp, 400, "username, bookId, quantity required"); return;
                }
                int qty = Integer.parseInt(qtyStr);
                ctx.getCartDAO().updateQuantity(user, bookId, qty);
                json(resp, "{\"success\":true}");

            } else {
                error(resp, 404, "Unknown PUT path: " + path);
            }

        } catch (Exception e) {
            System.err.println("[PUT] ERROR: " + e.getMessage());
            e.printStackTrace();
            error(resp, 500, e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";
        setCors(resp);
        resp.setContentType("application/json; charset=UTF-8");
        System.out.println("[DELETE] " + path);

        try {
            if (path.matches("/users/[^/]+")) {
                String username = path.substring(7);
                ctx.getUserDAO().delete(username);
                json(resp, "{\"success\":true}");

            } else if (path.equals("/cart")) {
                String user   = req.getParameter("user");
                String bookId = req.getParameter("bookId");
                if (user == null || user.isBlank()) {
                    error(resp, 400, "user parameter required"); return;
                }
                if (bookId != null && !bookId.isBlank()) {
                    ctx.getCartDAO().removeItem(user, bookId);
                } else {
                    ctx.getCartDAO().clearCart(user);
                }
                json(resp, "{\"success\":true}");

            } else {
                error(resp, 404, "Unknown DELETE path: " + path);
            }

        } catch (Exception e) {
            System.err.println("[DELETE] ERROR: " + e.getMessage());
            e.printStackTrace();
            error(resp, 500, e.getMessage());
        }
    }

    // ── OPTIONS (CORS preflight) ───────────────────────────────────────────

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCors(resp);
        resp.setStatus(204);
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    private void handleLogin(String body, HttpServletResponse resp) throws IOException {
        String username = extractJson(body, "username");
        String password = extractJson(body, "password");

        if (username == null || password == null) {
            error(resp, 400, "username and password required"); return;
        }

        Optional<Person> person = ctx.getUserDAO().findByUsername(username.trim());
        if (person.isEmpty() || !person.get().verifyPassword(password)) {
            error(resp, 401, "Invalid username or password.");
            return;
        }

        Person p = person.get();
        System.out.println("[Login] Success: " + p.getUsername() + " role=" + p.getRole());
        json(resp, String.format(
            "{\"success\":true,\"username\":\"%s\",\"name\":\"%s\",\"role\":\"%s\"}",
            p.getUsername(), p.getName(), p.getRole()
        ));
    }

    private void handleRegister(String body, HttpServletResponse resp) throws IOException {
        String username = extractJson(body, "username");
        String password = extractJson(body, "password");
        String name     = extractJson(body, "name");
        String email    = extractJson(body, "email");

        if (username == null || password == null || name == null || email == null) {
            error(resp, 400, "All fields are required."); return;
        }
        if (password.length() < 6) {
            error(resp, 400, "Password must be at least 6 characters."); return;
        }
        if (ctx.getUserDAO().usernameExists(username)) {
            error(resp, 409, "Username already exists."); return;
        }

        User newUser = new User(username.trim(), password, name.trim(), email.trim());
        ctx.getUserDAO().save(newUser);
        System.out.println("[Register] New user: " + username);
        json(resp, "{\"success\":true,\"message\":\"Account created.\"}");
    }

    private void handleAddBook(String body, HttpServletResponse resp) throws IOException {
        String id     = "B" + System.currentTimeMillis();
        String cat    = extractJson(body, "cat");
        String title  = extractJson(body, "title");
        String author = extractJson(body, "author");
        String priceS = extractJson(body, "price");
        String pagesS = extractJson(body, "pages");
        String stockS = extractJson(body, "stock");
        String img    = extractJson(body, "img");
        String desc   = extractJson(body, "desc");

        if (title == null || author == null || priceS == null || stockS == null) {
            error(resp, 400, "title, author, price, stock required"); return;
        }

        double price = Double.parseDouble(priceS);
        int    pages = pagesS != null ? Integer.parseInt(pagesS) : 200;
        int    stock = Integer.parseInt(stockS);
        if (img == null || img.isBlank()) {
            img = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300&h=400&fit=crop";
        }

        Book book = new Book(id, cat != null ? cat : "novels",
                             title, author, price, pages, stock,
                             img, desc != null ? desc : "");
        ctx.getBookDAO().save(book);
        System.out.println("[AddBook] Added: " + title);
        json(resp, "{\"success\":true,\"id\":\"" + id + "\"}");
    }

    private void handleAddToCart(String body, HttpServletResponse resp) throws IOException {
        String username = extractJson(body, "username");
        String bookId   = extractJson(body, "bookId");
        String qtyStr   = extractJson(body, "quantity");

        if (username == null || bookId == null) {
            error(resp, 400, "username and bookId required"); return;
        }
        int qty = (qtyStr != null) ? Integer.parseInt(qtyStr) : 1;
        ctx.getCartDAO().addOrUpdate(username, bookId, qty);
        json(resp, "{\"success\":true}");
    }

    private void handleCheckout(String body, HttpServletResponse resp) throws IOException {
        String username = extractJson(body, "username");
        if (username == null) { error(resp, 400, "username required"); return; }

        List<CartItem> cartItems = ctx.getCartDAO().getCart(username);
        if (cartItems.isEmpty()) { error(resp, 400, "Cart is empty."); return; }

        double subtotal = 0;
        List<CartItemSnapshot> snapshots = new ArrayList<>();
        for (CartItem ci : cartItems) {
            Optional<Book> book = ctx.getBookDAO().findById(ci.getBookId());
            if (book.isPresent()) {
                Book b = book.get();
                subtotal += ci.lineTotal(b.getPrice());
                snapshots.add(new CartItemSnapshot(
                    b.getId(), b.getTitle(), b.getPrice(), ci.getQuantity()
                ));
            }
        }

        Order order = new Order(username, snapshots, subtotal, ctx.getShippingCost());
        ctx.getOrderDAO().save(order);
        ctx.getCartDAO().clearCart(username);
        System.out.println("[Checkout] Order placed: " + order.getId() + " total=" + order.getTotal());
        json(resp, "{\"success\":true,\"orderId\":\"" + order.getId() +
                   "\",\"total\":" + order.getTotal() + "}");
    }

    private void handleAddReview(String body, HttpServletResponse resp) throws IOException {
        String username  = extractJson(body, "username");
        String category  = extractJson(body, "category");
        String bookTitle = extractJson(body, "bookTitle");
        String text      = extractJson(body, "text");
        String ratingStr = extractJson(body, "rating");

        if (username == null || category == null || bookTitle == null
                || text == null || ratingStr == null) {
            error(resp, 400, "All review fields required"); return;
        }

        int rating = Integer.parseInt(ratingStr);
        Review review = new Review(username, category, bookTitle, rating, text);
        ctx.getReviewDAO().save(review);
        System.out.println("[Review] Saved: " + username + " -> " + bookTitle);
        json(resp, "{\"success\":true}");
    }

    private void handleStats(HttpServletResponse resp) throws IOException {
        int    books   = ctx.getBookDAO().findAll().size();
        int    users   = ctx.getUserDAO().findAllUsers().size();
        int    orders  = ctx.getOrderDAO().findAll().size();
        int    reviews = ctx.getReviewDAO().findAll().size();
        double revenue = ctx.getOrderDAO().findAll().stream()
                           .mapToDouble(Order::getTotal).sum();
        json(resp, String.format(
            "{\"books\":%d,\"users\":%d,\"orders\":%d,\"reviews\":%d,\"revenue\":%.0f}",
            books, users, orders, reviews, revenue
        ));
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private void json(HttpServletResponse resp, String json) throws IOException {
        resp.setStatus(200);
        resp.getWriter().write(json);
        resp.getWriter().flush();
    }

    private void error(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        String safe = (msg != null ? msg.replace("\"", "'") : "Unknown error");
        resp.getWriter().write("{\"error\":\"" + safe + "\"}");
        resp.getWriter().flush();
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    // ── JSON helpers ──────────────────────────────────────────────────────

    /**
     * Extract a value from a simple flat JSON string.
     * Handles both string values ("key":"val") and numeric values ("key":123).
     */
    private String extractJson(String json, String key) {
        if (json == null || json.isBlank()) return null;

        // String value
        String strSearch = "\"" + key + "\":\"";
        int s = json.indexOf(strSearch);
        if (s != -1) {
            s += strSearch.length();
            int e = json.indexOf("\"", s);
            if (e != -1) return json.substring(s, e).replace("\\\"", "\"");
        }

        // Numeric value
        String numSearch = "\"" + key + "\":";
        s = json.indexOf(numSearch);
        if (s != -1) {
            s += numSearch.length();
            // Skip whitespace
            while (s < json.length() && json.charAt(s) == ' ') s++;
            int e = s;
            while (e < json.length() &&
                   (Character.isDigit(json.charAt(e)) || json.charAt(e) == '.' || json.charAt(e) == '-'))
                e++;
            if (e > s) return json.substring(s, e);
        }

        return null;
    }

    private <T extends BaseEntity> String listToJson(List<T> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toJson());
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String cartToJson(List<CartItem> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toJson());
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
