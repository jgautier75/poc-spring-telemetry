package com.acme.jga.ports.services.api.users;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.ports.dtos.search.v1.SearchFilterDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.users.v1.UserDisplayDto;
import com.acme.jga.ports.dtos.users.v1.UserDto;
import com.acme.jga.ports.dtos.users.v1.UsersDisplayListDto;
import io.opentelemetry.api.trace.Span;

public interface UserPortService {

    UidDto createUser(String tenantUid, String orgUid, UserDto userDto, Span parentSpan) throws FunctionalException;

    Integer updateUser(String tenantUid, String orgUid, String userUid, UserDto userDto, Span parentSpan) throws FunctionalException;

    Integer deleteUser(String tenantUid, String orgUid, String userUid, Span parentSpan) throws FunctionalException;

    UserDisplayDto findUser(String tenantUid, String orgUid, String userUid, Span parentSpan) throws FunctionalException;

    UsersDisplayListDto filterUsers(String tenantUid, String orgUid, SearchFilterDto searchFilter, Span span) throws FunctionalException;
}
