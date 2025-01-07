package com.acme.jga.ports.services.impl.spi;

import com.acme.jga.domain.functions.users.api.UserFind;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.user.UsersPortConverter;
import com.acme.jga.ports.dtos.spi.v1.UserInfosDto;
import com.acme.jga.ports.services.api.spi.ISpiService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.services.impl.user.UserPortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpiService extends AbstractPortService implements ISpiService {
    private static final String INSTRUMENTATION_NAME = UserPortService.class.getCanonicalName();
    private final UserFind userFind;
    private final UsersPortConverter usersPortConverter;

    @Autowired
    public SpiService(OpenTelemetryWrapper openTelemetryWrapper, UserFind userFind, UsersPortConverter usersPortConverter) {
        super(openTelemetryWrapper);
        this.userFind = userFind;
        this.usersPortConverter = usersPortConverter;
    }

    @Override
    public Optional<UserInfosDto> findByCriteria(String field, String value) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_SPI_FIND", null, (span) -> {
            Optional<User> optDomainUser = Optional.empty();
            if ("email".equalsIgnoreCase(field)) {
                optDomainUser = userFind.byEmail(value, null);
            } else if ("uid".equalsIgnoreCase(field)) {
                optDomainUser = userFind.byUid(value, null);
            } else if ("login".equalsIgnoreCase(field)) {
                optDomainUser = userFind.byLogin(value, null);
            }
            Optional<UserInfosDto> userInfosDto = Optional.empty();
            if (optDomainUser.isPresent()) {
                userInfosDto = usersPortConverter.convertUserDomainToSpi(optDomainUser.get());
            }
            return userInfosDto;
        });
    }
}
