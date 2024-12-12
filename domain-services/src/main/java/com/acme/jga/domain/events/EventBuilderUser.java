package com.acme.jga.domain.events;

import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserCommons;
import com.acme.jga.domain.model.v1.UserCredentials;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventBuilderUser {

    private static final String META_COMMONS_PREFIX = "";

    public List<AuditChange> buildAuditsChange(User previous, User current) {
        final List<AuditChange> auditChanges = new ArrayList<>();
        DiffResult<User> diffResult = previous.diff(current);
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

        // Build audit changes for user commons
        if (current != null) {
            List<AuditChange> userCommonsChanges = buildUserCommonsAuditChange(previous.getCommons(), current.getCommons());
            if (!userCommonsChanges.isEmpty()) {
                auditChanges.addAll(userCommonsChanges);
            }
            List<AuditChange> userCredentialsChanges = buildUserCredentialsChange(previous.getCredentials(), current.getCredentials());
            if (!userCredentialsChanges.isEmpty()) {
                auditChanges.addAll(userCredentialsChanges);
            }
        }

        return auditChanges;
    }

    private List<AuditChange> buildUserCredentialsChange(UserCredentials previous, UserCredentials current) {
        final List<AuditChange> auditChanges = new ArrayList<>();
        DiffResult<UserCredentials> diffs = previous.diff(current);
        diffs.forEach(diff -> {
            List<AuditChange> diffChanges = processAuditChanges(diff);
            if (!CollectionUtils.isEmpty(diffChanges)) {
                auditChanges.addAll(diffChanges);
            }
        });
        return auditChanges;
    }

    private List<AuditChange> buildUserCommonsAuditChange(UserCommons previous, UserCommons current) {
        final List<AuditChange> auditChanges = new ArrayList<>();
        DiffResult<UserCommons> diffs = previous.diff(current);
        diffs.forEach(diff -> {
            List<AuditChange> diffChanges = processAuditChanges(diff);
            if (!CollectionUtils.isEmpty(diffChanges)) {
                auditChanges.addAll(diffChanges);
            }
        });
        return auditChanges;
    }

    private List<AuditChange> processAuditChanges(Diff<?> diff) {
        final List<AuditChange> auditChanges = new ArrayList<>();
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
        return auditChanges;
    }

}
