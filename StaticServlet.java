package com.bookstore.server;

import jakarta.servlet.http.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Serves static files (HTML, CSS, JS) from the resources/static directory.
 * Disables browser caching so fresh JS/CSS is always loaded after rebuilds.
 */
public class StaticServlet extends HttpServlet {

    private static final Map<String, String> MIME_TYPES = Map.of(
        "html", "text/html; charset=UTF-8",
        "css",  "text/css",
        "js",   "application/javascript",
        "png",  "image/png",
        "jpg",  "image/jpeg",
        "ico",  "image/x-icon"
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) path = "/index.html";

        // Try to load from classpath: resources/static/<path>
        String resourcePath = "/static" + path;
        URL resource = getClass().getResource(resourcePath);

        if (resource == null) {
            // SPA fallback — any unknown path gets index.html
            resource = getClass().getResource("/static/index.html");
        }

        if (resource == null) {
            resp.sendError(404, "Not found: " + path);
            return;
        }

        String ext = path.contains(".")
            ? path.substring(path.lastIndexOf('.') + 1)
            : "html";
        resp.setContentType(MIME_TYPES.getOrDefault(ext, "text/plain"));

        // No-cache headers — prevents browser from serving stale JS/CSS
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma",        "no-cache");
        resp.setHeader("Expires",       "0");

        try (InputStream in = resource.openStream()) {
            byte[] buf = in.readAllBytes();
            resp.setContentLength(buf.length);
            resp.getOutputStream().write(buf);
        }
    }
}
