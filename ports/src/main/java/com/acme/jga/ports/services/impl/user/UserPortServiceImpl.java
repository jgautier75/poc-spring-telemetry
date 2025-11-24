package com.acme.jga.ports.services.impl.user;

import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.api.*;
import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.user.UsersPortConverter;
import com.acme.jga.ports.dtos.search.v1.SearchFilterDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.users.v1.UserDisplayDto;
import com.acme.jga.ports.dtos.users.v1.UserDto;
import com.acme.jga.ports.dtos.users.v1.UsersDisplayListDto;
import com.acme.jga.ports.services.api.users.UserPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.validation.users.UsersValidationEngine;
import com.acme.jga.search.filtering.parser.QueryParser;
import com.acme.jga.search.filtering.utils.ParsingResult;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import io.opentelemetry.api.trace.Span;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserPortServiceImpl extends AbstractPortService implements UserPortService {
    private static final String INSTRUMENTATION_NAME = UserPortServiceImpl.class.getCanonicalName();

    private final UserCreate userCreate;
    private final UserUpdate userUpdate;
    private final UserDelete userDelete;
    private final UserFind userFind;
    private final UserFilter userFilter;
    private final UsersPortConverter usersConverter;
    private final UsersValidationEngine usersValidationEngine;
    private final QueryParser queryParser = new QueryParser();

    @Autowired
    public UserPortServiceImpl(TenantFind tenantFind, OrganizationFind organizationFind, UserCreate userCreate,
                               UsersPortConverter usersConverter, UsersValidationEngine usersValidationEngine,
                               OpenTelemetryWrapper openTelemetryWrapper, UserUpdate userUpdate, UserDelete userDelete,
                               UserFind userFind, UserFilter userFilter) {
        super(openTelemetryWrapper);
        this.userCreate = userCreate;
        this.userUpdate = userUpdate;
        this.userDelete = userDelete;
        this.userFind = userFind;
        this.userFilter = userFilter;
        this.usersConverter = usersConverter;
        this.usersValidationEngine = usersValidationEngine;
    }

    @Override
    public UidDto createUser(String tenantUid, String orgUid, UserDto userDto, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_CREATE", parentSpan, (span) -> {
            usersValidationEngine.validate(userDto);
            User user = usersConverter.convertUserDtoToDomain(userDto);
            CompositeId compositeId = userCreate.execute(tenantUid, orgUid, user, span);
            return new UidDto(compositeId.getUid());
        });
    }

    @Override
    public Integer updateUser(String tenantUid, String orgUid, String userUid, UserDto userDto, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_CREATE", parentSpan, (span) -> {
            userDto.setUid(userUid);
            usersValidationEngine.validate(userDto);
            User user = usersConverter.convertUserDtoToDomain(userDto);
            return userUpdate.execute(tenantUid, orgUid, user, span);
        });
    }


    @Override
    public Integer deleteUser(String tenantUid, String orgUid, String userUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_DELETE", parentSpan, (span) -> userDelete.execute(tenantUid, orgUid, userUid, span));
    }

    @Override
    public UserDisplayDto findUser(String tenantUid, String orgUid, String userUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_FIND_UID", parentSpan, (span) -> {
            User user = userFind.byUid(tenantUid, orgUid, userUid, span);
            return usersConverter.convertUserDomainToDisplay(user);
        });
    }

    @Override
    public UsersDisplayListDto filterUsers(String tenantUid, String orgUid, SearchFilterDto searchFilter, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_FILTER", parentSpan, (span) -> {

            // Search/filter users
            Map<String, Object> searchParams = buildSearchParams(searchFilter);
            PaginatedResults<UserDisplay> paginatedResults = userFilter.execute(tenantUid, orgUid, span, searchParams);

            // Convert and return
            List<UserDisplayDto> lightUsers = paginatedResults.getResults().stream().map(usersConverter::convertUserDisplayToDto).toList();
            return new UsersDisplayListDto(paginatedResults.getNbResults(), paginatedResults.getNbPages(), paginatedResults.getPageIndex(), paginatedResults.getPageSize(), lightUsers);
        });
    }

    private @NotNull Map<String, Object> buildSearchParams(SearchFilterDto searchFilter) {
        ParsingResult parsingResult = queryParser.parseQuery(searchFilter.getFilter());
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put(FilteringConstants.PAGE_INDEX, searchFilter.getPageIndex());
        searchParams.put(FilteringConstants.PAGE_SIZE, searchFilter.getPageSize());
        searchParams.put(FilteringConstants.PARSING_RESULTS, parsingResult);
        searchParams.put(FilteringConstants.ORDER_BY, searchFilter.getOrderBy());
        return searchParams;
    }

}
