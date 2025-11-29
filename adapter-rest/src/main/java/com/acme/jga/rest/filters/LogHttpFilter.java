package com.acme.jga.rest.filters;

import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.logging.utils.CachedHttpServletRequest;
import com.acme.jga.logging.utils.LogHttpUtils;
import com.acme.jga.rest.config.AppDebuggingProperties;
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
    private final ILoggingFacade loggingFacade;
    private final AppDebuggingProperties appConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        boolean debugModeActivated = activateDebugging(httpServletRequest);
        ScopedValue.where(LogHttpUtils.LOG_FLAG, debugModeActivated).run(
                () -> {
                    if (debugModeActivated) {
                        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
                        try {
                            CachedHttpServletRequest cacheHttpServletRequest = new CachedHttpServletRequest(httpServletRequest);
                            doDebugRequest(cacheHttpServletRequest);
                            filterChain.doFilter(cacheHttpServletRequest, responseWrapper);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            doDebugResponse(responseWrapper);
                            try {
                                responseWrapper.copyBodyToResponse();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        try {
                            filterChain.doFilter(httpServletRequest, httpServletResponse);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
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
            activateDebugMode = debugHeaderMatch(request);
        }
        return activateDebugMode;
    }

    /**
     * Does debug header matches http servlet request headers.
     *
     * @param request Http servlet request
     * @return Boolean
     */
    private boolean debugHeaderMatch(HttpServletRequest request) {
        boolean debug = false;
        if (!ObjectUtils.isEmpty(appConfig.getHeaderName()) && !ObjectUtils.isEmpty(appConfig.getDebugValue())) {
            String headerValue = request.getHeader(appConfig.getHeaderName());
            debug = !ObjectUtils.isEmpty(headerValue) && appConfig.getDebugValue().equals(headerValue);
        }
        return debug;
    }

    /**
     * Debug http request.
     *
     * @param httpServletRequest Http request
     */
    public void doDebugRequest(CachedHttpServletRequest httpServletRequest) {
        String dumpHttpRequest = LogHttpUtils.dumpHttpRequest(httpServletRequest);
        String callerName = this.getClass().getName() + "-doDebugFiler";
        loggingFacade.debugS(callerName, getRequestResponseLogHeader("REQUEST_BEGINS"), new Object[0]);
        loggingFacade.debugS(callerName, dumpHttpRequest, new Object[0]);
        loggingFacade.debugS(callerName, getRequestResponseLogHeader("REQUEST_ENDS"), new Object[0]);
    }

    /**
     * Debug http response
     *
     * @param httpServletResponse Http response
     */
    public void doDebugResponse(ContentCachingResponseWrapper httpServletResponse) {
        String callerName = this.getClass().getName() + "-doDebugResponse";
        String dumpHttpResponse = LogHttpUtils.dumpHttpResponse(httpServletResponse);
        loggingFacade.debugS(callerName, getRequestResponseLogHeader("RESPONSE_BEGINS"), new Object[0]);
        loggingFacade.debugS(callerName, dumpHttpResponse, new Object[0]);
        loggingFacade.debugS(callerName, getRequestResponseLogHeader("RESPONSE_ENDS"), new Object[0]);
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
