package com.acme.jga.domain.model.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class Sector implements Diffable<Sector> {
    private Long id;
    private String uid;
    private String code;
    private String label;
    private Long orgId;
    private boolean root;
    private Long parentId;
    private String parentUid;
    private Long tenantId;
    private List<Sector> children;

    public void addChild(Sector sector) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(sector);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public Sector withId(Long id) {
        this.id = id;
        return this;
    }

    public Sector withUid(String uid) {
        this.uid = uid;
        return this;
    }

    public Sector withTenantId(Long id) {
        this.tenantId = id;
        return this;
    }

    public Sector withOrgId(Long id) {
        this.orgId = id;
        return this;
    }

    public Sector withParentId(Long id) {
        this.parentId = id;
        return this;
    }

    @Override
    public DiffResult<Sector> diff(Sector obj) {
        DiffBuilder<Sector> builder = new DiffBuilder.Builder<Sector>()
                .setLeft(this)
                .setRight(obj)
                .setStyle(ToStringStyle.SHORT_PREFIX_STYLE)
                .build();
        return builder.append("code", this.code, obj.code)
                .append("label", this.label, obj.label)
                .append("parentUid", this.parentUid, obj.parentUid)
                .build();
    }
}
