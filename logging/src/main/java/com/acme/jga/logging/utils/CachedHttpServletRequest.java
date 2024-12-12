package com.acme.jga.logging.utils;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Http servlet request with cached body.
 */
public class CachedHttpServletRequest extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        try (InputStream requestInputStream = request.getInputStream()) {
            this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
        }
    }

    public CachedHttpServletRequest(HttpServletRequest request, byte[] cachedBody) {
        super(request);
        this.cachedBody = cachedBody;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedInputStream(this.cachedBody);
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }

}
