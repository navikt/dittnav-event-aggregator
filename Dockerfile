FROM navikt/java:17-appdynamics
COPY init.sh /init-scripts/init.sh
COPY build/libs/dittnav-event-aggregator-all.jar /app/app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"
ENV PORT=8080
EXPOSE $PORT
