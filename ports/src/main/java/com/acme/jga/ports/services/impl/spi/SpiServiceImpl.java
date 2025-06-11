package com.acme.jga.ports.services.impl.spi;

import com.acme.jga.domain.functions.users.api.UserFind;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.user.UsersPortConverter;
import com.acme.jga.ports.dtos.spi.v1.UserInfosDto;
import com.acme.jga.ports.services.api.spi.SpiService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.services.impl.user.UserPortServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpiServiceImpl extends AbstractPortService implements SpiService {
    private static final String INSTRUMENTATION_NAME = UserPortServiceImpl.class.getCanonicalName();
    private final UserFind userFind;
    private final UsersPortConverter usersPortConverter;

    @Autowired
    public SpiServiceImpl(OpenTelemetryWrapper openTelemetryWrapper, UserFind userFind, UsersPortConverter usersPortConverter) {
        super(openTelemetryWrapper);
        this.userFind = userFind;
        this.usersPortConverter = usersPortConverter;
    }

    @Override
    public Optional<UserInfosDto> findByCriteria(String field, String value) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_SPI_FIND", (span) -> {
            Optional<User> optDomainUser = Optional.empty();
            if ("email".equalsIgnoreCase(field)) {
                optDomainUser = userFind.byEmail(value);
            } else if ("uid".equalsIgnoreCase(field)) {
                optDomainUser = userFind.byUid(value);
            } else if ("login".equalsIgnoreCase(field)) {
                optDomainUser = userFind.byLogin(value);
            }
            Optional<UserInfosDto> userInfosDto = Optional.empty();
            if (optDomainUser.isPresent()) {
                userInfosDto = usersPortConverter.convertUserDomainToSpi(optDomainUser.get());
            }
            return userInfosDto;
        });
    }
}
