package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.dtos.search.v1.SearchFilterDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.users.v1.UserDisplayDto;
import com.acme.jga.ports.dtos.users.v1.UserDto;
import com.acme.jga.ports.dtos.users.v1.UsersDisplayListDto;
import com.acme.jga.ports.services.api.users.UserPortService;
import com.acme.jga.rest.versioning.WebApiVersions;
import com.acme.jga.utils.otel.OtelContext;
import com.acme.jga.utils.string.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UsersController extends AbstractController {
    private static final String INSTRUMENTATION_NAME = UsersController.class.getCanonicalName();
    private final ILoggingFacade loggingFacade;
    private final UserPortService userPortService;

    protected UsersController(OpenTelemetryWrapper openTelemetryWrapper, UserPortService userPortService, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper);
        this.userPortService = userPortService;
        this.loggingFacade = loggingFacade;
    }

    @PostMapping(value = WebApiVersions.UsersResourceVersion.ROOT)
    public ResponseEntity<UidDto> createUser(@PathVariable("tenantUid") String tenantUid,
                                             @PathVariable(value = "orgUid") String orgUid,
                                             @RequestBody UserDto userDto) throws FunctionalException {
        UidDto uidDto = withSpan(INSTRUMENTATION_NAME, "API_USERS_CREATE", (span) -> userPortService.createUser(tenantUid, orgUid, userDto, span));
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @PostMapping(value = WebApiVersions.UsersResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateUser(@PathVariable("tenantUid") String tenantUid,
                                           @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid,
                                           @RequestBody UserDto userDto) throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_USERS_UPDATE", (span) -> userPortService.updateUser(tenantUid, orgUid, userUid, userDto, span));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(WebApiVersions.UsersResourceVersion.ROOT)
    public ResponseEntity<UsersDisplayListDto> filterUsers(@PathVariable("tenantUid") String tenantUid,
                                                           @PathVariable("orgUid") String orgUid,
                                                           @RequestParam(value = "filter", required = false) String searchFilter,
                                                           @RequestParam(value = "index", required = false, defaultValue = "1") Integer pageIndex,
                                                           @RequestParam(value = "size", required = false, defaultValue = "10") Integer pageSize,
                                                           @RequestParam(value = "orderBy", required = false, defaultValue = "label") String orderBy) throws FunctionalException {
        SearchFilterDto searchFilterDto = new SearchFilterDto(searchFilter, pageSize, pageIndex, orderBy);
        UsersDisplayListDto users = withSpan(INSTRUMENTATION_NAME, "API_USERS_LIST", (span) -> {
            loggingFacade.infoS(INSTRUMENTATION_NAME, "Find users for tenant [%s], organization [%s], filter [%s]", new Object[]{tenantUid, orgUid, StringUtils.nvl(searchFilter)}, OtelContext.fromSpan(span));
            return userPortService.filterUsers(tenantUid, orgUid, searchFilterDto, span);
        });
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(WebApiVersions.UsersResourceVersion.WITH_UID)
    public ResponseEntity<UserDisplayDto> findUser(@PathVariable("tenantUid") String tenantUid,
                                                   @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid) throws FunctionalException {
        UserDisplayDto userDisplayDto = withSpan(INSTRUMENTATION_NAME, "API_USERS_FIND", (span) -> userPortService.findUser(tenantUid, orgUid, userUid, span));
        return new ResponseEntity<>(userDisplayDto, HttpStatus.OK);
    }

    @DeleteMapping(WebApiVersions.UsersResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteUser(@PathVariable("tenantUid") String tenantUid,
                                           @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid) throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_USERS_DELETE", (span) -> userPortService.deleteUser(tenantUid, orgUid, userUid, span));
        return ResponseEntity.noContent().build();
    }

}
