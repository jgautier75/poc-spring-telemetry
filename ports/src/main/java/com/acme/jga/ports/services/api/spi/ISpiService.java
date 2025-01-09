package com.acme.jga.ports.services.api.spi;

import com.acme.jga.ports.dtos.spi.v1.UserInfosDto;
import io.opentelemetry.api.trace.Span;

import java.util.Optional;

public interface ISpiService {
    Optional<UserInfosDto> findByCriteria(String field, String value, Span parentSpan);
}
