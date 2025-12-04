package com.acme.jga.rest.controllers;

import com.acme.jga.rest.annotations.MetricPoint;
import com.acme.jga.rest.pojo.MetricDefinition;
import com.acme.jga.rest.pojo.MetricsConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.classgraph.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyze source code, search methods annotated with @MetricPoint and generate a json file as output.
 */
@ExtendWith(MockitoExtension.class)
public class ClassGraphMetricsTest {
    public static final Logger LOGGER = LoggerFactory.getLogger("app-logger");

    @org.junit.jupiter.api.Test
    @Disabled
    public void metricsPointTest() throws JsonProcessingException {
        String metricString = MetricPoint.class.getName();
        final List<MetricDefinition> definitions = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo() // Scan classes, methods, fields, annotations
                .enableAnnotationInfo()
                .scan()) { // Start the scan

            for (ClassInfo clazz : scanResult.getClassesWithMethodAnnotation(metricString)) {                
                clazz.getMethodInfo().forEach(mi -> {
                    AnnotationInfo annotationInfo = mi.getAnnotationInfo(metricString);
                    if (annotationInfo != null) {                        
                        MetricDefinition metricDefinition = new MetricDefinition();
                        AnnotationParameterValueList annotationParameterValueList = annotationInfo.getParameterValues();
                        annotationParameterValueList.forEach(av -> {
                            if (av.getName().equals("alias")) {
                                metricDefinition.setTag((String) av.getValue());
                            } else if (av.getName().equals("version")) {
                                metricDefinition.setVersion((String) av.getValue());
                            } else if (av.getName().equals("method")) {
                                metricDefinition.setHttpVerb((String) av.getValue());
                            } else if (av.getName().equals("regex")) {
                                metricDefinition.setUriRegex((String) av.getValue());
                            }                            
                        });
                        definitions.add(metricDefinition);
                    }
                });
            }
        }
        if (!CollectionUtils.isEmpty(definitions)) {
            MetricsConfig metricsConfig = new MetricsConfig(definitions);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonMetrics = objectMapper.writeValueAsString(metricsConfig);
            LOGGER.info(jsonMetrics);
        }
    }

}
