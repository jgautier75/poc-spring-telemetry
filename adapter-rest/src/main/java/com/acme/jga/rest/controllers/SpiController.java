package com.acme.jga.rest.controllers;

import com.acme.jga.ports.dtos.spi.v1.UserInfosDto;
import com.acme.jga.ports.services.api.spi.ISpiService;
import com.acme.jga.rest.versioning.WebApiVersions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SpiController {
    private final ISpiService spiService;

    @GetMapping(value = WebApiVersions.SpiResourceVersion.FIND_USER)
    public ResponseEntity<UserInfosDto> fetchUser(@RequestParam(value = "field", required = false) String field,
                                                  @RequestParam(value = "value", required = false) String value) {
        Optional<UserInfosDto> optUser = spiService.findByCriteria(field,value);
        return optUser.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

}
