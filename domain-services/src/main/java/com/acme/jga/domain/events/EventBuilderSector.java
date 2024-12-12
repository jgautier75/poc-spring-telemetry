package com.acme.jga.domain.events;

import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.v1.Sector;
import org.apache.commons.lang3.builder.DiffResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventBuilderSector {

    public List<AuditChange> buildAuditsChange(Sector previous, Sector current) {
        final List<AuditChange> auditChanges = new ArrayList<>();
        DiffResult<Sector> diffResult = previous.diff(current);
        diffResult.forEach(diff -> {
            AuditOperation operation = AuditOperation.UPDATE;
            if (diff.getLeft() != null && diff.getRight() == null) {
                operation = AuditOperation.REMOVE;
            } else if (diff.getLeft() == null && diff.getRight() != null) {
                operation = AuditOperation.ADD;
            }
            switch (operation) {
                case ADD:
                   auditChanges.add(new AuditChange(diff.getFieldName(), operation, null, (String) diff.getRight()));
                    break;
                case UPDATE:
                    auditChanges.add(new AuditChange(diff.getFieldName(), operation, (String) diff.getLeft(), (String) diff.getRight()));
                    break;
                case REMOVE:
                    auditChanges.add(new AuditChange(diff.getFieldName(), operation, (String) diff.getLeft(), null));
                    break;
                default:
                    break;
            }
        });
        return auditChanges;
    }

}
