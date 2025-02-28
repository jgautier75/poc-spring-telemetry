package com.acme.jga.domain.events;

import com.acme.jga.logging.services.api.ILoggingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Service
@RequiredArgsConstructor
public class EventBusErrorHandler implements ErrorHandler {
    private final ILoggingFacade loggingFacade;

    @Override
    public void handleError(Throwable t) {
        loggingFacade.error(this.getClass().getName(), t);
    }

}
