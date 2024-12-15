package com.acme.jga.rest.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class MetricDefinition {
    private String tag;
    private String httpVerb;
    private String uriRegex;   
    private String version; 
}
