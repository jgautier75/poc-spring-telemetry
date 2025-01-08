package com.acme.jga.domain.functions.sectors.api;

import com.acme.jga.domain.model.v1.Sector;
import io.opentelemetry.api.trace.Span;

public interface SectorHierarchy {

    Sector execute(String tenantUid, String organizationUid, Span parentSpan);

}
