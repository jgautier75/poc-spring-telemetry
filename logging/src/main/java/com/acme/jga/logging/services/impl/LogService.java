package com.acme.jga.logging.services.impl;

import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.logging.utils.LogHttpUtils;
import com.acme.jga.utils.http.RequestCorrelationId;
import com.acme.jga.utils.lambdas.StreamUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class LogService implements ILogService {
    public static final Logger LOGGER = LoggerFactory.getLogger("app-logger");
    private final MessageSource messageSource;
    private static final Function<Object, Object> F_DECODE = object -> {
        if (String.class.isAssignableFrom(object.getClass())) {
            try {
                return URLDecoder.decode((String) object, StandardCharsets.UTF_8);
            } catch (Exception e) {
                // Decoding error, just ignore
                // Can fail for example if an invalid email id filled in, expected email regex
                // is returned but this regex contains an ampersand !
            }
        }
        return object;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void infoB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.INFO, bundleMessage, params, callerName);
    }

    @Override
    public void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void infoS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.INFO, message, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debugB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.DEBUG, bundleMessage, params, callerName);
    }

    @Override
    public void debug(String message) {
        if (LogHttpUtils.LOG_FLAG.get() != null && LogHttpUtils.LOG_FLAG.get()) {
            LOGGER.debug(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debugS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.DEBUG, message, params, callerName);
    }

    @Override
    public void trace(String msg) {
        LOGGER.trace(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void traceB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.TRACE, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void traceS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.TRACE, message, params, callerName);
    }

    @Override
    public void warn(String msg) {
        LOGGER.warn(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warnB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.WARN, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warnS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.WARN, message, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String callerName, Exception e) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(callerName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void errorB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.ERROR, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void errorS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.ERROR, message, params, callerName);
    }

    /**
     * Log with bundle message.
     *
     * @param level      Log level
     * @param message    Message key in bundle
     * @param params     Parameters
     * @param callerName Caller
     */
    public void logBundleMessage(Level level, String message, Object[] params, String callerName) {
        String bundleMessage = buildMessage(true, message, params, callerName);
        if (level == Level.DEBUG && LOGGER.isDebugEnabled() && LogHttpUtils.LOG_FLAG.get() != null && LogHttpUtils.LOG_FLAG.get()) {
            LOGGER.debug(bundleMessage);
        } else if (level == Level.ERROR && LOGGER.isErrorEnabled()) {
            LOGGER.error(bundleMessage);
        } else if (level == Level.INFO && LOGGER.isInfoEnabled()) {
            LOGGER.info(bundleMessage);
        } else if (level == Level.TRACE && LOGGER.isTraceEnabled()) {
            LOGGER.trace(bundleMessage);
        } else if (level == Level.WARN && LOGGER.isWarnEnabled()) {
            LOGGER.warn(bundleMessage);
        }
    }

    /**
     * Log using string format.<br/>
     * Use standard %s for substitution
     *
     * @param level      Log level
     * @param message    Message
     * @param params     Parameters
     * @param callerName Caller name
     */
    public void logSimpleMessage(Level level, String message, Object[] params, String callerName) {
        String bundleMessage = buildMessage(false, message, params, callerName);
        if (level == Level.DEBUG && LOGGER.isDebugEnabled() && LogHttpUtils.LOG_FLAG.get() != null && LogHttpUtils.LOG_FLAG.get()) {
            LOGGER.debug(bundleMessage);
        } else if (level == Level.ERROR && LOGGER.isErrorEnabled()) {
            LOGGER.error(bundleMessage);
        } else if (level == Level.INFO && LOGGER.isInfoEnabled()) {
            LOGGER.info(bundleMessage);
        } else if (level == Level.TRACE && LOGGER.isTraceEnabled()) {
            LOGGER.trace(bundleMessage);
        } else if (level == Level.WARN && LOGGER.isWarnEnabled()) {
            LOGGER.warn(bundleMessage);
        }
    }

    /**
     * Build message.
     *
     * @param bundleMessage Is message a bundle key
     * @param msg           Message to format
     * @param params        Parameters
     * @param callerName    Caller name
     * @return Message
     */
    public String buildMessage(boolean bundleMessage, String msg, Object[] params, String callerName) {
        String targetMsg;
        Object[] filteredParams = StreamUtil.ofNullableArray(params).map(F_DECODE).toList().toArray();
        if (bundleMessage) {
            targetMsg = messageSource.getMessage(msg, filteredParams, LocaleContextHolder.getLocale());
        } else {
            targetMsg = String.format(msg, filteredParams);
        }
        return messageSource.getMessage("log_msg_noctx_format", new Object[]{RequestCorrelationId.correlationKey(), callerName, targetMsg}, LocaleContextHolder.getLocale());
    }

    @Override
    public void error(String callerName, Throwable t) {
        LOGGER.error(callerName, t);
    }

}
