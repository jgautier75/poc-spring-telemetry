package com.acme.jga.logging.bundle;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BundleFactory {
    private final MessageSource messageSource;

    public String getMessage(String code,Object[] parameters){
        return messageSource.getMessage(code,parameters,LocaleContextHolder.getLocale());
    }
}
