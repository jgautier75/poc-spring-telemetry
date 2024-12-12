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
public class UserCommons implements Serializable, IVersioned, Diffable<UserCommons> {
    @Serial
    private static final long serialVersionUID = 6525190905413054113L;
    private static final String PREFIX = "commons_";
    private String firstName;
    private String lastName;
    private String middleName;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }

    @Override
    public DiffResult<UserCommons> diff(UserCommons obj) {
        return new DiffBuilder<>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(PREFIX + "firstName", this.firstName, obj.firstName)
                .append(PREFIX + "lastName", this.lastName, obj.lastName)
                .append(PREFIX + "middleName", this.middleName, obj.middleName)
                .build();
    }
}
