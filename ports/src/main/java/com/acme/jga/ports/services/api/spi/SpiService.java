package com.acme.jga.ports.services.api.spi;

import com.acme.jga.ports.dtos.spi.v1.UserInfosDto;

import java.util.Optional;

public interface SpiService {
    Optional<UserInfosDto> findByCriteria(String field, String value);
}
