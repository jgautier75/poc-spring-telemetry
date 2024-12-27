package com.acme.jga.ports.dtos.organizations.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class OrganizationListLightDto {
    private Integer nbResults;
    private Integer nbPages;
    private Integer pageIndex;
    private Integer pageSize;
    private List<OrganizationLightDto> organizations;
}
