package com.acme.jga.rest.filters;

import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.logging.utils.CachedHttpServletRequest;
import com.acme.jga.logging.utils.LogHttpUtils;
import com.acme.jga.rest.config.AppDebuggingConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LogHttpFilter extends OncePerRequestFilter {
    private final ILogService logService;
    private final AppDebuggingConfig appConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        boolean debugModeActivated = activateDebugging(httpServletRequest);
        if (debugModeActivated) {
            LogHttpUtils.APP_LOG_CTX.set(true);
            CachedHttpServletRequest cacheHttpServletRequest = new CachedHttpServletRequest(httpServletRequest);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
            try {
                doDebugRequest(cacheHttpServletRequest);
                filterChain.doFilter(cacheHttpServletRequest, responseWrapper);
            } finally {
                doDebugResponse(responseWrapper);
                responseWrapper.copyBodyToResponse();
                LogHttpUtils.APP_LOG_CTX.remove();
            }
        } else {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    /**
     * Check if debug mode must be activated or not for request.
     *
     * @param request Http request
     * @return Boolean flag
     */
    private boolean activateDebugging(HttpServletRequest request) {
        String reqUri = request.getRequestURI();
        boolean activateDebugMode = appConfig.isForceDebugMode();

        if (!activateDebugMode && !reqUri.contains("actuator")) {
            if (!ObjectUtils.isEmpty(appConfig.getHeaderName()) && !ObjectUtils.isEmpty(appConfig.getDebugValue())) {
                String headerValue = request.getHeader(appConfig.getHeaderName());
                activateDebugMode = !ObjectUtils.isEmpty(headerValue) && appConfig.getDebugValue().equals(headerValue);
            }
        }
        return activateDebugMode;
    }

    /**
     * Debug http request.
     *
     * @param httpServletRequest Http request
     */
    public void doDebugRequest(CachedHttpServletRequest httpServletRequest) {
        String dumpHttpRequest = LogHttpUtils.dumpHttpRequest(httpServletRequest);
        String callerName = this.getClass().getName() + "-doDebugFiler";
        logService.debugS(callerName, getRequestResponseLogHeader("REQUEST_BEGINS"), new Object[0]);
        logService.debugS(callerName, dumpHttpRequest, new Object[0]);
        logService.debugS(callerName, getRequestResponseLogHeader("REQUEST_ENDS"), new Object[0]);
    }

    /**
     * Debug http response
     *
     * @param httpServletResponse Http response
     */
    public void doDebugResponse(ContentCachingResponseWrapper httpServletResponse) {
        String callerName = this.getClass().getName() + "-doDebugResponse";
        String dumpHttpResponse = LogHttpUtils.dumpHttpResponse(httpServletResponse);
        logService.debugS(callerName, getRequestResponseLogHeader("RESPONSE_BEGINS"), new Object[0]);
        logService.debugS(callerName, dumpHttpResponse, new Object[0]);
        logService.debugS(callerName, getRequestResponseLogHeader("RESPONSE_ENDS"), new Object[0]);
    }

    /**
     * Build response trace.
     *
     * @param requestResponse Http response
     * @return Response trace
     */
    private String getRequestResponseLogHeader(String requestResponse) {
        return "**************************** " + requestResponse + " ****************************\n\r";
    }

}
