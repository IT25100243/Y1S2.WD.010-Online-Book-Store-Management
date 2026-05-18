package com.bookstore.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Application entry point.
 * Starts an embedded Jetty HTTP server on port 8080.
 *
 * Routes:
 *   /api/*  → ApiServlet  (REST JSON backend)
 *   /*      → StaticServlet (HTML/CSS/JS frontend)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8080;

        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(
            ServletContextHandler.SESSIONS
        );
        context.setContextPath("/");

        // Register API servlet for all /api/* routes
        context.addServlet(new ServletHolder(new ApiServlet()), "/api/*");

        // Register static file servlet for everything else
        context.addServlet(new ServletHolder(new StaticServlet()), "/*");

        server.setHandler(context);
        server.start();

        System.out.println("========================================");
        System.out.println("  📚 PageTurner Bookstore is running!");
        System.out.println("  Open: http://localhost:" + port);
        System.out.println("  Admin login: admin / admin123");
        System.out.println("========================================");

        server.join();
    }
}
