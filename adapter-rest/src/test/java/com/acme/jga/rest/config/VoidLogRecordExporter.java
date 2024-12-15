package com.acme.jga.rest.config;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

import java.util.Collection;

public class VoidLogRecordExporter implements LogRecordExporter {

    @Override
    public CompletableResultCode export(Collection<LogRecordData> logs) {
        return new CompletableResultCode().succeed();
    }

    @Override
    public CompletableResultCode flush() {
        return new CompletableResultCode().succeed();
    }

    @Override
    public CompletableResultCode shutdown() {
        return new CompletableResultCode().succeed();
    }

}
