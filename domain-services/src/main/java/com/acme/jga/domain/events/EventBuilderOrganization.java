package com.acme.jga.domain.events;

import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.v1.OrganizationCommons;
import org.apache.commons.lang3.builder.DiffResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventBuilderOrganization {
    private static final String META_COMMONS_PREFIX = "commons.";

    public List<AuditChange> buildAuditsChange(OrganizationCommons previous, OrganizationCommons current) {
        final List<AuditChange> auditChanges = new ArrayList<>();
        DiffResult<OrganizationCommons> diffResult = previous.diff(current);
        diffResult.forEach(diff -> {
            AuditOperation operation = AuditOperation.UPDATE;
            if (diff.getLeft() != null && diff.getRight() == null) {
                operation = AuditOperation.REMOVE;
            } else if (diff.getLeft() == null && diff.getRight() != null) {
                operation = AuditOperation.ADD;
            }
            switch (operation) {
                case ADD:
                    auditChanges.add(new AuditChange(META_COMMONS_PREFIX + diff.getFieldName(), operation, null, (String) diff.getRight()));
                    break;
                case UPDATE:
                    auditChanges.add(new AuditChange(META_COMMONS_PREFIX + diff.getFieldName(), operation, (String) diff.getLeft(), (String) diff.getRight()));
                    break;
                case REMOVE:
                    auditChanges.add(new AuditChange(META_COMMONS_PREFIX + diff.getFieldName(), operation, (String) diff.getLeft(), null));
                    break;
                default:
                    break;
            }
        });
        return auditChanges;
    }

}
