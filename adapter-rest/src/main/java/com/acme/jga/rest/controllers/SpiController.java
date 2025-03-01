package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.dtos.spi.v1.UserInfosDto;
import com.acme.jga.ports.services.api.spi.SpiService;
import com.acme.jga.rest.versioning.WebApiVersions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class SpiController extends AbstractController {
    private final SpiService spiService;

    protected SpiController(OpenTelemetryWrapper openTelemetryWrapper, SpiService spiService) {
        super(openTelemetryWrapper);
        this.spiService = spiService;
    }

    @GetMapping(value = WebApiVersions.SpiResourceVersion.FIND_USER)
    public ResponseEntity<UserInfosDto> fetchUser(@RequestParam(value = "field", required = false) String field,
                                                  @RequestParam(value = "value", required = false) String value) throws FunctionalException {
        Optional<UserInfosDto> optUser = withSpan(SpiController.class.getCanonicalName(), "SPI_FILTER", (span) -> spiService.findByCriteria(field, value, span));
        return optUser.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

}
