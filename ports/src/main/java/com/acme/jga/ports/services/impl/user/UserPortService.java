package com.acme.jga.ports.services.impl.user;

import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.api.*;
import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.user.UsersPortConverter;
import com.acme.jga.ports.dtos.search.v1.SearchFilterDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.users.v1.UserDisplayDto;
import com.acme.jga.ports.dtos.users.v1.UserDto;
import com.acme.jga.ports.dtos.users.v1.UsersDisplayListDto;
import com.acme.jga.ports.services.api.users.IUserPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.validation.users.UsersValidationEngine;
import com.acme.jga.search.filtering.parser.QueryParser;
import com.acme.jga.search.filtering.utils.ParsingResult;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserPortService extends AbstractPortService implements IUserPortService {
    private static final String INSTRUMENTATION_NAME = UserPortService.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final UserCreate userCreate;
    private final UserUpdate userUpdate;
    private final UserDelete userDelete;
    private final UserFind userFind;
    private final UserFilter userFilter;
    private final UsersPortConverter usersConverter;
    private final UsersValidationEngine usersValidationEngine;
    private final QueryParser queryParser = new QueryParser();

    @Autowired
    public UserPortService(TenantFind tenantFind, OrganizationFind organizationFind, UserCreate userCreate,
                           UsersPortConverter usersConverter, UsersValidationEngine usersValidationEngine,
                           OpenTelemetryWrapper openTelemetryWrapper, UserUpdate userUpdate, UserDelete userDelete,
                           UserFind userFind, UserFilter userFilter) {
        super(openTelemetryWrapper);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.userCreate = userCreate;
        this.userUpdate = userUpdate;
        this.userDelete = userDelete;
        this.userFind = userFind;
        this.userFilter = userFilter;
        this.usersConverter = usersConverter;
        this.usersValidationEngine = usersValidationEngine;
    }

    @Override
    public UidDto createUser(String tenantUid, String orgUid, UserDto userDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_CREATE", null, (span) -> {
            ValidationResult validationResult = usersValidationEngine.validate(userDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            User user = usersConverter.convertUserDtoToDomain(userDto);
            CompositeId compositeId = userCreate.execute(tenantUid, orgUid, user, span);
            return new UidDto(compositeId.getUid());
        });
    }

    @Override
    public Integer updateUser(String tenantUid, String orgUid, String userUid, UserDto userDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_CREATE", null, (span) -> {
            userDto.setUid(userUid);
            ValidationResult validationResult = usersValidationEngine.validate(userDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            User user = usersConverter.convertUserDtoToDomain(userDto);
            return userUpdate.execute(tenantUid, orgUid, user, span);
        });
    }


    @Override
    public Integer deleteUser(String tenantUid, String orgUid, String userUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_DELETE", null, (span) -> userDelete.execute(tenantUid, orgUid, userUid, span));
    }

    @Override
    public UserDisplayDto findUser(String tenantUid, String orgUid, String userUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_FIND_UID", null, (span) -> {
            User user = userFind.byUid(tenantUid, orgUid, userUid, span);
            return usersConverter.convertUserDomainToDisplay(user);
        });
    }

    @Override
    public UsersDisplayListDto filterUsers(String tenantUid, String orgUid, SearchFilterDto searchFilter) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_USERS_FILTER", null, (span) -> {
            // Ensure tenant exists
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            // Ensure organization exists
            Organization org = organizationFind.byTenantIdAndUid(tenant.getId(), orgUid, false, span);

            // Search/filter users
            Map<String, Object> searchParams = buildSearchParams(searchFilter);
            PaginatedResults<User> paginatedResults = userFilter.execute(tenant.getId(), org.getId(), span, searchParams);

            // Convert and return
            List<UserDisplayDto> lightUsers = new ArrayList<>();
            paginatedResults.getResults().forEach(usr -> lightUsers.add(usersConverter.convertUserDomainToDisplay(usr)));
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
