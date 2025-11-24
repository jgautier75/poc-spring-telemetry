package com.acme.jga.domain.model.v1;

import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.api.MainApiVersion;
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
public class OrganizationCommons implements IVersioned, Serializable, Diffable<OrganizationCommons> {
    @Serial
    private static final long serialVersionUID = 6339219514949636784L;
    private String code;
    private String label;
    private OrganizationKind kind;
    private String country;
    private OrganizationStatus status;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }

    @Override
    public DiffResult<OrganizationCommons> diff(OrganizationCommons obj) {
        DiffBuilder<OrganizationCommons> builder = new DiffBuilder.Builder<OrganizationCommons>()
                .setLeft(this)
                .setRight(obj)
                .setStyle(ToStringStyle.SHORT_PREFIX_STYLE)
                .build();
        builder.append("code", this.code, obj.code)
                .append("label", this.label, obj.label)
                .append("kind", this.kind, obj.kind)
                .append("country", this.country, obj.country)
                .append("status", this.status, obj.status)
                .build();
        return builder.build();
    }
}
