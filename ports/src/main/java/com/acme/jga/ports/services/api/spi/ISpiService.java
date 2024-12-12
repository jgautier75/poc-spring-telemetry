package com.acme.jga.ports.services.api.spi;

import com.acme.jga.ports.port.spi.v1.UserInfosDto;

import java.util.Optional;

public interface ISpiService {
    Optional<UserInfosDto> findByCriteria(String field, String value);
}
