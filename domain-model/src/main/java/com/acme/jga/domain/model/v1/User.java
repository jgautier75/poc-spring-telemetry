package com.acme.jga.domain.model.v1;

import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.api.IVersioned;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class User implements Serializable, IVersioned, Diffable<User> {
    @Serial
    private static final long serialVersionUID = -2560819667784907502L;
    private Long id;
    private String uid;
    private Long tenantId;
    private Long organizationId;
    private UserCredentials credentials;
    private UserCommons commons;
    private UserStatus status;
    private Organization organization;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }

    @Override
    public DiffResult<User> diff(User obj) {
        return new DiffBuilder<>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("status", this.status, obj.status)
                .build();
    }
}
