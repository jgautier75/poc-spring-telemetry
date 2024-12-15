package com.acme.jga.rest.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MetricsConfig {
    private List<MetricDefinition> metricsDefinitions;
}
