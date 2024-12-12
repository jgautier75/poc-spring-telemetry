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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class Tenant implements IVersioned, Diffable<Tenant> {
    private Long id;
    private String uid;
    private String code;
    private String label;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }


    @Override
    public DiffResult<Tenant> diff(Tenant obj) {
        return new DiffBuilder<>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("label", this.label, obj.label)
                .build();
    }
}
