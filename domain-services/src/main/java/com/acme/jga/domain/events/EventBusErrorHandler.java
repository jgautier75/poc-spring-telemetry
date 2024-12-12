package com.acme.jga.domain.events;

import com.acme.jga.logging.services.api.ILogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Service
@RequiredArgsConstructor
public class EventBusErrorHandler implements ErrorHandler {
    private final ILogService logService;

    @Override
    public void handleError(Throwable t) {
        logService.error(this.getClass().getName(), t);
    }

}
