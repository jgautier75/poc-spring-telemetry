package com.acme.jga.ports.dtos.dependencies.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DependencyDto implements Serializable {
    private String name;
    private String version;
}
