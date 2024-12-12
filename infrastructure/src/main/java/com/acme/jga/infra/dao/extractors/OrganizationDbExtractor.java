package com.acme.jga.infra.dao.extractors;

import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.jdbc.utils.SQLExtractor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrganizationDbExtractor {
	public static OrganizationDb extractOrganization(ResultSet resultSet, boolean checkNext) throws SQLException {
		OrganizationDb org = null;
		if (!checkNext || resultSet.next()) {
			org = OrganizationDb.builder().country(SQLExtractor.extractString(resultSet, "country"))
					.id(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ID))
					.code(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_CODE))
					.kind(OrganizationKind.fromIntValue(SQLExtractor.extractInteger(resultSet, "kind")))
					.label(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_LABEL))
					.status(OrganizationStatus.fromIntValue(SQLExtractor.extractInteger(resultSet, "status")))
					.tenantId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_TENANT_ID))
					.uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID)).build();
		}
		return org;
	}
}
