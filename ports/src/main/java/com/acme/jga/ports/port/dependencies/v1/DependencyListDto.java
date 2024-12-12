package com.acme.jga.ports.port.dependencies.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DependencyListDto implements Serializable {
    private List<DependencyDto> dependencies;

    public void addDependency(String key, String value) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(new DependencyDto(key, value));
    }
}
