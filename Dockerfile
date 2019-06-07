FROM navikt/java:12
COPY build/libs/dittnav-event-aggregator.jar /app/app.jar
EXPOSE 8080
