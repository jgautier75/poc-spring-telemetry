package com.acme.jga.ports.port.users.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class UsersDisplayListDto {
    private Integer nbResults;
    private Integer nbPages;
    private Integer pageIndex;
    private Integer pageSize;
    private List<UserDisplayDto> users;
}
