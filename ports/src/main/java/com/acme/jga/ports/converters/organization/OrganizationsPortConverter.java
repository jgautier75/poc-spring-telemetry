package com.acme.jga.ports.converters.organization;

import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.OrganizationCommons;
import com.acme.jga.ports.converters.sectors.SectorsPortConverter;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationCommonsDto;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationDto;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationLightDto;
import com.acme.jga.ports.dtos.sectors.v1.SectorDisplayDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrganizationsPortConverter {
    private final SectorsPortConverter sectorsPortConverter;

    public Organization convertOrganizationDtoToDomain(OrganizationDto organizationDto) {
        Organization organization = null;
        if (organizationDto != null) {
            organization = new Organization();
            organization.setCommons(convertOrganizationCommonDtoToDomain(organizationDto.getCommons()));
        }
        return organization;
    }

    public OrganizationDto convertOrganizationToDto(Organization org) {
        return Optional.ofNullable(org).map(o -> {
            OrganizationDto dto = OrganizationDto.builder()
                    .commons(convertOrganizationCommonsToDto(org.getCommons()))
                    .id(org.getId())
                    .uid(org.getUid())
                    .build();
            Optional.ofNullable(org.getSector()).ifPresent(sect -> {
                SectorDisplayDto sectorDisplayDto = sectorsPortConverter.convertSectorDomainToSectorDisplay(sect);
                dto.setSectors(sectorDisplayDto);
            });
            return dto;
        }).orElse(null);
    }

    public OrganizationCommons convertOrganizationCommonDtoToDomain(OrganizationCommonsDto organizationCommonsDto) {
        return Optional.ofNullable(organizationCommonsDto).map(o -> OrganizationCommons.builder()
                        .code(organizationCommonsDto.getCode())
                        .country(organizationCommonsDto.getCountry())
                        .kind(organizationCommonsDto.getKind())
                        .label(organizationCommonsDto.getLabel())
                        .status(organizationCommonsDto.getStatus()).build())
                .orElse(null);
    }

    public OrganizationCommonsDto convertOrganizationCommonsToDto(OrganizationCommons organizationCommons) {
        return Optional.ofNullable(organizationCommons).map(o -> OrganizationCommonsDto.builder()
                        .code(organizationCommons.getCode())
                        .country(organizationCommons.getCountry())
                        .kind(organizationCommons.getKind())
                        .label(organizationCommons.getLabel())
                        .status(organizationCommons.getStatus()).build())
                .orElse(null);
    }

    public OrganizationLightDto convertOrganizationToLightOrgDto(Organization organization) {
        return Optional.ofNullable(organization).map(o -> OrganizationLightDto.builder()
                        .kind(organization.getCommons().getKind())
                        .label(organization.getCommons().getLabel())
                        .status(organization.getCommons().getStatus())
                        .uid(organization.getUid())
                        .code(organization.getCommons().getCode()).build())
                .orElse(null);
    }

}
