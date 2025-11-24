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
public class UserCredentials implements Serializable, IVersioned, Diffable<UserCredentials> {
    @Serial
    private static final long serialVersionUID = 8824656658346637238L;
    private static final String PREFIX = "credentials_";
    private String login;
    private String email;
    private String defaultPassword;
    private String encryptedPassword;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }

    @Override
    public DiffResult<UserCredentials> diff(UserCredentials obj) {
        DiffBuilder<UserCredentials> builder = new DiffBuilder.Builder<UserCredentials>()
                .setLeft(this)
                .setRight(obj)
                .setStyle(ToStringStyle.SHORT_PREFIX_STYLE)
                .build();
        builder.append(PREFIX + "login", this.login, obj.login);
        builder.append(PREFIX + "login", this.login, obj.login);
        return builder.build();
    }
}
