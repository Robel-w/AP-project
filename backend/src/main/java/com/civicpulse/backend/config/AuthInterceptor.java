package com.civicpulse.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // 1. Allow public auth paths
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/me") || path.equals("/api/auth/logout")) {
            return true;
        }

        // 2. Allow public read-only paths (GET method only)
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            if (path.startsWith("/api/feedback") || path.startsWith("/api/files/download")) {
                return true;
            }
        }

        // 3. For all other paths, check active session with a valid user attribute
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return true;
        }

        // Otherwise reject
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized: Please log in first.\"}");
        return false;
    }
}
