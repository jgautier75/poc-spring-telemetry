package com.acme.jga.rest.filters;

import com.acme.jga.utils.http.RequestCorrelationId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(0)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            RequestCorrelationId.CORRELATION_KEY.set(request.getHeader("X-CORRELATION-KEY"));
            filterChain.doFilter(request, response);
        } finally {
            RequestCorrelationId.CORRELATION_KEY.remove();
        }
    }
}
