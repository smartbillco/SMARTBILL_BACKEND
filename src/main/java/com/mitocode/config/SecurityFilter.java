package com.mitocode.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class SecurityFilter implements Filter {

    @Value("${server.allowed-ips:}")
    private String[] allowedIps;

    private static final Pattern[] MALICIOUS_PATTERNS = {
            Pattern.compile(".*[\\x00-\\x1F\\x7F].*"), // Caracteres de control y DEL
            Pattern.compile(".*(\\r|\\n|%0D|%0A|%0a|%0d).*"), // CR/LF injection
            Pattern.compile(".*[<>\"'\\\\].*"), // Caracteres especiales HTML/XML
            Pattern.compile(".*(/\\.\\./|\\\\\\.\\.\\\\).*"), // Path traversal
            Pattern.compile(".*%.*%.*"), // Doble encoding
            Pattern.compile(".*\\|.*"), // Pipe injection
            Pattern.compile(".*`.*") // Command injection
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Validar IP si está configurado
        if (allowedIps.length > 0 && !isIpAllowed(httpRequest.getRemoteAddr())) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "IP not allowed");
            return;
        }

        // Validar patrones maliciosos en URL y parámetros
        if (containsMaliciousPatterns(httpRequest)) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isIpAllowed(String ip) {
        for (String allowedIp : allowedIps) {
            if (allowedIp.equals(ip) || allowedIp.equals("*")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsMaliciousPatterns(HttpServletRequest request) {
        // Verificar URI
        String uri = request.getRequestURI().toLowerCase();
        for (Pattern pattern : MALICIOUS_PATTERNS) {
            if (pattern.matcher(uri).matches()) {
                return true;
            }
        }

        // Verificar parámetros
        for (String paramName : request.getParameterMap().keySet()) {
            for (String paramValue : request.getParameterValues(paramName)) {
                for (Pattern pattern : MALICIOUS_PATTERNS) {
                    if (pattern.matcher(paramValue.toLowerCase()).matches()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}