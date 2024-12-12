package com.acme.jga.infra.converters;

import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.OrganizationCommons;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrganizationsInfraConverter {

    public OrganizationDb convertOrganizationToOrganizationDb(Organization organization) {
        return Optional.ofNullable(organization).map(o -> {
            OrganizationDb organizationDb = new OrganizationDb();
            Optional.ofNullable(organization.getCommons()).ifPresent(c -> {
                organizationDb.setCode(organization.getCommons().getCode());
                organizationDb.setCountry(organization.getCommons().getCountry());
                organizationDb.setId(organization.getId());
                organizationDb.setKind(organization.getCommons().getKind());
                organizationDb.setLabel(organization.getCommons().getLabel());
                organizationDb.setStatus(organization.getCommons().getStatus());
                organizationDb.setTenantId(organization.getTenantId());
                organizationDb.setUid(organization.getUid());
            });
            return organizationDb;
        }).orElse(null);
    }

    public Organization convertOrganizationDbToOrganization(OrganizationDb orgDb) {
        return Optional.ofNullable(orgDb).map(o -> {
            Organization org = new Organization();
            org.setTenantId(orgDb.getTenantId());
            org.setUid(orgDb.getUid());
            org.setId(orgDb.getId());
            OrganizationCommons commons = OrganizationCommons.builder()
                    .code(orgDb.getCode())
                    .country(orgDb.getCountry())
                    .kind(orgDb.getKind())
                    .label(orgDb.getLabel())
                    .status(orgDb.getStatus())
                    .build();
            org.setCommons(commons);
            return org;
        }).orElse(null);
    }

}
