Application launch command with OpenTelemetry:


-javaagent:/home/jgautier/git-data/opentelemetry-javaagent.jar -Dotel.service.name=poc-st -Dotel.traces.exporter=jaeger -Dotel.exporter.otlp.protocol=http/protobuf -Dotel.javaagent.debug=true -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=none