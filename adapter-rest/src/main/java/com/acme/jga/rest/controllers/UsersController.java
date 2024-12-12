package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.ports.port.search.v1.SearchFilterDto;
import com.acme.jga.ports.port.shared.UidDto;
import com.acme.jga.ports.port.users.v1.UserDisplayDto;
import com.acme.jga.ports.port.users.v1.UserDto;
import com.acme.jga.ports.port.users.v1.UsersDisplayListDto;
import com.acme.jga.ports.services.api.users.IUserPortService;
import com.acme.jga.rest.versioning.WebApiVersions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UsersController {
    private final IUserPortService userPortService;

    @PostMapping(value = WebApiVersions.UsersResourceVersion.ROOT)
    public ResponseEntity<UidDto> createUser(@PathVariable("tenantUid") String tenantUid,
                                             @PathVariable(value = "orgUid") String orgUid,
                                             @RequestBody UserDto userDto) throws FunctionalException {
        UidDto uidDto = userPortService.createUser(tenantUid, orgUid, userDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @PostMapping(value = WebApiVersions.UsersResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateUser(@PathVariable("tenantUid") String tenantUid,
                                           @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid,
                                           @RequestBody UserDto userDto) throws FunctionalException {
        userPortService.updateUser(tenantUid, orgUid, userUid, userDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(WebApiVersions.UsersResourceVersion.ROOT)
    public ResponseEntity<UsersDisplayListDto> listUsers(@PathVariable("tenantUid") String tenantUid,
                                                         @PathVariable("orgUid") String orgUid,
                                                         @RequestParam(value = "filter", required = false) String searchFilter,
                                                         @RequestParam(value = "index", required = false, defaultValue = "1") Integer pageIndex,
                                                         @RequestParam(value = "size", required = false, defaultValue = "10") Integer pageSize,
                                                         @RequestParam(value = "orderBy", required = false, defaultValue = "label") String orderBy) throws FunctionalException {
        SearchFilterDto searchFilterDto = new SearchFilterDto(searchFilter, pageSize, pageIndex, orderBy);
        UsersDisplayListDto users = userPortService.filterUsers(tenantUid, orgUid, searchFilterDto);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(WebApiVersions.UsersResourceVersion.WITH_UID)
    public ResponseEntity<UserDisplayDto> findUser(@PathVariable("tenantUid") String tenantUid,
                                                   @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid) throws FunctionalException {
        UserDisplayDto userDisplayDto = userPortService.findUser(tenantUid, orgUid, userUid);
        return new ResponseEntity<>(userDisplayDto, HttpStatus.OK);
    }

    @DeleteMapping(WebApiVersions.UsersResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteUser(@PathVariable("tenantUid") String tenantUid,
                                           @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid) throws FunctionalException {
        userPortService.deleteUser(tenantUid, orgUid, userUid);
        return ResponseEntity.noContent().build();
    }

}
