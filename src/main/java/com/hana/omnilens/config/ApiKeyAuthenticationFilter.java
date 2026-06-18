package com.hana.omnilens.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hana.omnilens.common.api.ApiResponse;
import com.hana.omnilens.common.exception.ErrorCode;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-HANA-OMNILENS-API-KEY";

    private final OmniLensSecurityProperties properties;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(OmniLensSecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.apiKeyEnabled() || isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!StringUtils.hasText(properties.apiKeySha256())) {
            writeError(response, ErrorCode.API_KEY_NOT_CONFIGURED);
            return;
        }

        String providedKey = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(providedKey) || !matchesConfiguredHash(providedKey)) {
            writeError(response, ErrorCode.INVALID_API_KEY);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
                || path.equals("/actuator/info")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        HttpStatus status = errorCode.status();
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error(status.value(), errorCode.code(), errorCode.message()));
    }

    private boolean matchesConfiguredHash(String providedKey) {
        byte[] expected = properties.apiKeySha256().trim().getBytes(StandardCharsets.UTF_8);
        byte[] actual = sha256Hex(providedKey).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }

    private String sha256Hex(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
