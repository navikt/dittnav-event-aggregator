FROM navikt/java:13-appdynamics
COPY build/libs/dittnav-event-aggregator.jar /app/app.jar
ENV PORT=8080
EXPOSE $PORT
